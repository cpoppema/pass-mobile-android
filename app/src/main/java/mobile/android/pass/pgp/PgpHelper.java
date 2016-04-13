package mobile.android.pass.pgp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.spongycastle.bcpg.ArmoredOutputStream;
import org.spongycastle.bcpg.HashAlgorithmTags;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.openpgp.PGPCompressedData;
import org.spongycastle.openpgp.PGPEncryptedData;
import org.spongycastle.openpgp.PGPEncryptedDataList;
import org.spongycastle.openpgp.PGPKeyPair;
import org.spongycastle.openpgp.PGPLiteralData;
import org.spongycastle.openpgp.PGPObjectFactory;
import org.spongycastle.openpgp.PGPPrivateKey;
import org.spongycastle.openpgp.PGPPublicKey;
import org.spongycastle.openpgp.PGPPublicKeyEncryptedData;
import org.spongycastle.openpgp.PGPSecretKey;
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
import org.spongycastle.util.io.Streams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.util.Date;
import java.util.Iterator;

/**
 * Class for generating PGP key pairs and decrypting data.
 */
public class PgpHelper {
    private static final String PUBLIC_KEY = "public";
    private static final String PUBLIC_KEY_NAME = "public_name";
    private static final String SECRET_KEY = "secret";
    private static final String SECRET_KEY_ID = "secret_id";
    private static final int KEY_PAIR_BITS = 2048;

    private Context mContext;
    private PGPSecretKeyRingCollection mSecretKeyCollection = null;
    private long mSecretKeyId = -1;

    // Make sure BouncyCastle is set as security provider.
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Constructor.
     * @param context
     */
    public PgpHelper(Context context) {
        mContext = context;
    }

    /**
     * Function to get the armored string representation of the public key.
     * @return
     */
    public String getPublicKeyString() {
        return PreferenceManager.getDefaultSharedPreferences(mContext).getString(PUBLIC_KEY, "");
    }

    public String getPublicKeyName() {
        return PreferenceManager.getDefaultSharedPreferences(mContext).getString(PUBLIC_KEY_NAME, "");
    }

    public String decrypt(String encryptedData, PGPPrivateKey privateKey) {
        try {
            // Convert the String to a InputStream.
            byte[] encryptedBytes = encryptedData.getBytes(Charset.forName("UTF-8"));
            InputStream encryptedInputStream = new ByteArrayInputStream(encryptedBytes);
            encryptedInputStream = PGPUtil.getDecoderStream(encryptedInputStream);
            // Create PGP Objects from the InputStream.
            PGPObjectFactory pgpObjectFactory = new JcaPGPObjectFactory(encryptedInputStream);

            // Define used objects.
            PGPEncryptedDataList encryptedDataList;
            PGPPublicKeyEncryptedData publicKeyEncryptedData = null;

            // Get first PGP Object.
            Object pgpObject = pgpObjectFactory.nextObject();

            // Make sure we have a PGPEncryptedDataList.
            if (pgpObject instanceof PGPEncryptedDataList) {
                encryptedDataList = (PGPEncryptedDataList) pgpObject;
            } else {
                encryptedDataList = (PGPEncryptedDataList) pgpObjectFactory.nextObject();
            }

            // Create iterator for all EncryptedDataObjects. A PGP Message can have multiple
            // EncryptedDataObjects with the same content only encrypted with different PublicKeys.
            Iterator encryptedDataIterator = encryptedDataList.getEncryptedDataObjects();

            boolean hasPrivateKeyMatch = false;

            // Loop all EncryptedDataObjects.
            while (encryptedDataIterator.hasNext()) {
                // Get the EncryptedData.
                publicKeyEncryptedData = (PGPPublicKeyEncryptedData) encryptedDataIterator.next();
                // Get the ID of the key that needs to be used for decrypting the data.
                long keyId = publicKeyEncryptedData.getKeyID();

                if (privateKey.getKeyID() == keyId) {
                    hasPrivateKeyMatch = true;
                    break;
                }
            }

            // No PrivateKey has been found to decrypt this message.
            if (!hasPrivateKeyMatch) {
                return "";
            }

            // Decrypt data with PrivateKey.
            InputStream decryptedData = publicKeyEncryptedData.getDataStream(new BcPublicKeyDataDecryptorFactory(privateKey));

            // Create PGPObjects for this decrypted data.
            pgpObjectFactory = new JcaPGPObjectFactory(decryptedData);

            // Get the decrypted message.
            Object decryptedMessage = pgpObjectFactory.nextObject();

            // Decompress the message if it is compressed.
            if (decryptedMessage instanceof PGPCompressedData) {
                PGPCompressedData compressedData = (PGPCompressedData) decryptedMessage;
                pgpObjectFactory = new JcaPGPObjectFactory(compressedData.getDataStream());

                decryptedMessage = pgpObjectFactory.nextObject();
            }

            // Convert the LiteralData steam into a String object.
            if (decryptedMessage instanceof PGPLiteralData) {
                PGPLiteralData literalData = (PGPLiteralData) decryptedMessage;
                return new String(Streams.readAll(literalData.getInputStream()), Charset.forName("UTF-8"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Function to generate a KeyPair.
     * @param name Name of the KeyPair.
     * @param password Password for encrypting the PrivateKey.
     */
    public void generateKeyPair(String name, String password) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "SC");
            generator.initialize(KEY_PAIR_BITS);
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
            String secretKeyString = new String(secretStream.toByteArray(), Charset.forName("UTF-8"));

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            prefs.edit()
                    .putString(PUBLIC_KEY, publicKeyString)
                    .putString(PUBLIC_KEY_NAME, name)
                    .putString(SECRET_KEY, secretKeyString)
                    .putLong(SECRET_KEY_ID, secretKey.getKeyID())
                    .apply();

            resetCachedValues();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Function to reset the cached values.
     */
    private void resetCachedValues() {
        mSecretKeyCollection = null;
    }

    /**
     * Function to get the SecretKeyCollection from the armored string of the secret key in
     * the storage.
     * @return
     */
    private PGPSecretKeyRingCollection getSecretKeyCollection() {
        if (mSecretKeyCollection == null) {
            try {
                String privateKeyArmored = PreferenceManager.getDefaultSharedPreferences(mContext)
                        .getString(SECRET_KEY, "");
                InputStream inputStream = new ByteArrayInputStream(
                        privateKeyArmored.getBytes(Charset.forName("UTF-8")));
                inputStream = PGPUtil.getDecoderStream(inputStream);

                mSecretKeyCollection = new PGPSecretKeyRingCollection(
                        inputStream, new JcaKeyFingerprintCalculator());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mSecretKeyCollection;
    }

    private long getSecretKeyId() {
        if (mSecretKeyId == -1) {
            mSecretKeyId = PreferenceManager.getDefaultSharedPreferences(mContext)
                    .getLong(SECRET_KEY_ID, 0);
        }
        return mSecretKeyId;
    }

    public PGPPrivateKey getPrivateKey(String password) {
        return getPrivateKey(getSecretKeyId(), password);
    }

    /**
     * Function to get the private key used for decrypting
     * @param keyId The key that is needed.
     * @param password Password to decrypt the SecretKey into a PrivateKey user for decrypting
     *                 messages.
     * @return
     */
    private PGPPrivateKey getPrivateKey(long keyId, String password) {
        try {
            PGPSecretKey secretKey = getSecretKeyCollection().getSecretKey(keyId);

            if (secretKey != null) {
                return secretKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider("SC").build(password.toCharArray()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}