package mobile.android.pass;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.cardemulation.CardEmulation;
import android.preference.PreferenceManager;
import android.util.Log;

import org.spongycastle.bcpg.ArmoredOutputStream;
import org.spongycastle.bcpg.HashAlgorithmTags;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.openpgp.PGPCompressedData;
import org.spongycastle.openpgp.PGPEncryptedData;
import org.spongycastle.openpgp.PGPEncryptedDataList;
import org.spongycastle.openpgp.PGPException;
import org.spongycastle.openpgp.PGPKeyPair;
import org.spongycastle.openpgp.PGPLiteralData;
import org.spongycastle.openpgp.PGPObjectFactory;
import org.spongycastle.openpgp.PGPOnePassSignatureList;
import org.spongycastle.openpgp.PGPPrivateKey;
import org.spongycastle.openpgp.PGPPublicKey;
import org.spongycastle.openpgp.PGPPublicKeyEncryptedData;
import org.spongycastle.openpgp.PGPPublicKeyRing;
import org.spongycastle.openpgp.PGPPublicKeyRingCollection;
import org.spongycastle.openpgp.PGPSecretKey;
import org.spongycastle.openpgp.PGPSecretKeyRing;
import org.spongycastle.openpgp.PGPSecretKeyRingCollection;
import org.spongycastle.openpgp.PGPSignature;
import org.spongycastle.openpgp.PGPUtil;
import org.spongycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.spongycastle.openpgp.operator.PGPDigestCalculator;
import org.spongycastle.openpgp.operator.bc.BcPublicKeyDataDecryptorFactory;
import org.spongycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.spongycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.spongycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.spongycastle.openpgp.operator.jcajce.JcaPGPKeyPair;
import org.spongycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.spongycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder;
import org.spongycastle.openpgp.operator.jcajce.JcePublicKeyDataDecryptorFactoryBuilder;
import org.spongycastle.util.encoders.Base64Encoder;
import org.spongycastle.util.io.Streams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by marco on 09/04/16.
 */
public class PgpHelper {
    private Context mContext;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public PgpHelper(Context context) {
        mContext = context;
    }

    public String getPublicKeyString() {
        return PreferenceManager.getDefaultSharedPreferences(mContext).getString("public", "");
    }

    private long getSecretKeyId() {
        return PreferenceManager.getDefaultSharedPreferences(mContext).getLong("secret_id", -1);
    }

    private PGPPrivateKey getPrivateKey(String password) {

        try {
            String privateKeyArmored = PreferenceManager.getDefaultSharedPreferences(mContext).getString("secret", "");
            InputStream inputStream = new ByteArrayInputStream(privateKeyArmored.getBytes(Charset.forName("UTF-8")));
            inputStream = PGPUtil.getDecoderStream(inputStream);

            PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(
                    inputStream, new JcaKeyFingerprintCalculator());

            long keyId = PreferenceManager.getDefaultSharedPreferences(mContext).getLong("secret_id", -1);

            PGPSecretKey secretKey = pgpSec.getSecretKey(keyId);

            if (secretKey != null) {
                return secretKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider("SC").build(password.toCharArray()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String decrypt(String encryptedData, String password) {
        try
        {
            // WORKING
//            byte[] encrypted = encryptedData.getBytes(Charset.forName("UTF-8"));
//            InputStream in = new ByteArrayInputStream(encrypted);
//            in = PGPUtil.getDecoderStream(in);
//            PGPObjectFactory        pgpF = new JcaPGPObjectFactory(in);
//            PGPEncryptedDataList    enc = (PGPEncryptedDataList)pgpF.nextObject();
//
//            PGPPublicKeyEncryptedData     pbe = (PGPPublicKeyEncryptedData)enc.get(0);
//
//            // Create for loop to find the message belonging to the key id.
//
//            PGPPrivateKey privateKey = getPrivateKey(password);
//
//            InputStream clear = pbe.getDataStream(new BcPublicKeyDataDecryptorFactory(privateKey));
//
//            PGPObjectFactory        pgpFact = new JcaPGPObjectFactory(clear);
//
//            Object       message = pgpFact.nextObject();
//
//            if (message instanceof PGPLiteralData) {
//                PGPLiteralData ld = (PGPLiteralData)message;
//
//                return new String(Streams.readAll(ld.getInputStream()), Charset.forName("UTF-8"));
//            }
            byte[] encryptedBytes = encryptedData.getBytes(Charset.forName("UTF-8"));
            InputStream encryptedInputStream = new ByteArrayInputStream(encryptedBytes);
            encryptedInputStream = PGPUtil.getDecoderStream(encryptedInputStream);
            PGPObjectFactory pgpObjectFactory = new JcaPGPObjectFactory(encryptedInputStream);
            PGPEncryptedDataList encryptedDataList;

            Object pgpObject = pgpObjectFactory.nextObject();

            if (pgpObject instanceof PGPEncryptedDataList) {
                encryptedDataList = (PGPEncryptedDataList) pgpObject;
            } else {
                encryptedDataList = (PGPEncryptedDataList) pgpObjectFactory.nextObject();
            }

            PGPPublicKeyEncryptedData publicKeyEncryptedData = null;

            Iterator encryptedDataIterator = encryptedDataList.getEncryptedDataObjects();
            PGPPrivateKey privateKey = null;

            while (encryptedDataIterator.hasNext()) {
                publicKeyEncryptedData = (PGPPublicKeyEncryptedData) encryptedDataIterator.next();
                long keyId = publicKeyEncryptedData.getKeyID();
                if (keyId == getSecretKeyId()) {
                    privateKey = getPrivateKey(password);
                }
            }

            if (privateKey == null) {
                return "";
            }

            InputStream decryptedData = publicKeyEncryptedData.getDataStream(new BcPublicKeyDataDecryptorFactory(privateKey));

            pgpObjectFactory = new JcaPGPObjectFactory(decryptedData);

            Object decryptedMessage = pgpObjectFactory.nextObject();

            // TODO: Check for compressed data.
            if (decryptedMessage instanceof PGPLiteralData) {
                PGPLiteralData literalData = (PGPLiteralData) decryptedMessage;
                return new String(Streams.readAll(literalData.getInputStream()), Charset.forName("UTF-8"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public void generateKeyPair(String name, String password) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "SC");
            generator.initialize(2048);
            KeyPair pair = generator.generateKeyPair();

            PGPDigestCalculator sha1Calc = new JcaPGPDigestCalculatorProviderBuilder().build().get(HashAlgorithmTags.SHA1);
            PGPKeyPair pgpPair = new JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, pair, new Date());

            PGPSecretKey secretKey = new PGPSecretKey(
                    PGPSignature.DEFAULT_CERTIFICATION,
                    pgpPair,
                    name,
                    sha1Calc,
                    null,
                    null,
                    new JcaPGPContentSignerBuilder(
                            pgpPair.getPublicKey().getAlgorithm(),
                            HashAlgorithmTags.SHA1),
                    new JcePBESecretKeyEncryptorBuilder(
                            PGPEncryptedData.CAST5, sha1Calc)
                            .setProvider("SC")
                            .build(password.toCharArray()));

            // Convert PGPPublicKey to armored string.
            ByteArrayOutputStream pubStream = new ByteArrayOutputStream();
            ArmoredOutputStream pubArmorStream = new ArmoredOutputStream(pubStream);
            secretKey.getPublicKey().encode(pubArmorStream);
            pubArmorStream.close();
            String publicKeyString = new String(pubStream.toByteArray(), Charset.forName("UTF-8"));

            // Convert PGPSecretKey to armored string.
            ByteArrayOutputStream secretStream = new ByteArrayOutputStream();
            ArmoredOutputStream secretArmorStream = new ArmoredOutputStream(secretStream);
            secretKey.encode(secretArmorStream);
            secretArmorStream.close();
            String privateKeyString = new String(secretStream.toByteArray(), Charset.forName("UTF-8"));

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            prefs.edit()
                    .putString("public", publicKeyString)
                    .putString("secret", privateKeyString)
                    .putLong("secret_id", secretKey.getKeyID())
                    .apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

//    PGPObjectFactory        pgpF = new PGPObjectFactory(in);
//    PGPEncryptedDataList    enc = (PGPEncryptedDataList)pgpF.nextObject();
//
//    PGPPBEEncryptedData     pbe = (PGPPBEEncryptedData)enc.get(0);
//
//    InputStream clear = pbe.getDataStream(passPhrase, "BC");
//
//    PGPObjectFactory        pgpFact = new PGPObjectFactory(clear);
//
//    PGPCompressedData       cData = (PGPCompressedData)pgpFact.nextObject();
//
//pgpFact = new PGPObjectFactory(cData.getDataStream());
//
//        PGPLiteralData          ld = (PGPLiteralData)pgpFact.nextObject();
