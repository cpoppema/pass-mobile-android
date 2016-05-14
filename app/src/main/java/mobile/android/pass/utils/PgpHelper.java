package mobile.android.pass.utils;

import android.content.Context;
import android.text.TextUtils;

import org.spongycastle.bcpg.ArmoredOutputStream;
import org.spongycastle.bcpg.HashAlgorithmTags;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.openpgp.PGPEncryptedData;
import org.spongycastle.openpgp.PGPException;
import org.spongycastle.openpgp.PGPKeyPair;
import org.spongycastle.openpgp.PGPPublicKey;
import org.spongycastle.openpgp.PGPSecretKey;
import org.spongycastle.openpgp.PGPSecretKeyRingCollection;
import org.spongycastle.openpgp.PGPSignature;
import org.spongycastle.openpgp.PGPUtil;
import org.spongycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.spongycastle.openpgp.operator.PGPDigestCalculator;
import org.spongycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.spongycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.spongycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.spongycastle.openpgp.operator.jcajce.JcaPGPKeyPair;
import org.spongycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.spongycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.Date;

/**
 * Class for generating PGP key pairs and decrypting data.
 */
public class PgpHelper {
    private static final int KEY_PAIR_BITS = 2048;

    private Context mContext;
    private PGPSecretKeyRingCollection mSecretKeyCollection = null;
    private StorageHelper mStorageHelper;
    private long mSecretKeyId = -1;

    // Make sure BouncyCastle is set as security provider.
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static PGPSecretKey generateKeyPair(String name, String password) {
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

//            // Convert PGPPublicKey to armored string.
//            ByteArrayOutputStream pubStream = new ByteArrayOutputStream();
//            ArmoredOutputStream pubArmorStream = new ArmoredOutputStream(pubStream);
//            secretKey.getArmoredPublicKey().encode(pubArmorStream);
//            pubArmorStream.close();
//            String publicKeyString = new String(pubStream.toByteArray(), Charset.forName("UTF-8"));
//
//            // Convert PGPSecretKey to armored string.
//            ByteArrayOutputStream secretStream = new ByteArrayOutputStream();
//            ArmoredOutputStream secretArmorStream = new ArmoredOutputStream(secretStream);
//            secretKey.encode(secretArmorStream);
//            secretArmorStream.close();
//            String secretKeyString = new String(secretStream.toByteArray(), Charset.forName("UTF-8"));

//            mStorageHelper.setPublicKeyName(name);
//            mStorageHelper.setPublicKey(publicKeyString);
//            mStorageHelper.setSecretKey(secretKeyString);
//            mStorageHelper.setSecretKeyId(secretKey.getKeyID());

            return secretKey;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (PGPException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String extractArmoredPublicKey(PGPSecretKey secretKey) {
        try {
            ByteArrayOutputStream pubStream = new ByteArrayOutputStream();
            ArmoredOutputStream pubArmorStream = new ArmoredOutputStream(pubStream);
            secretKey.getPublicKey().encode(pubArmorStream);
            pubArmorStream.close();
            return new String(pubStream.toByteArray(), Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String extractArmoredPrivateKey(PGPSecretKey secretKey) {
        try {
            ByteArrayOutputStream secretStream = new ByteArrayOutputStream();
            ArmoredOutputStream secretArmorStream = new ArmoredOutputStream(secretStream);
            secretKey.encode(secretArmorStream);
            secretArmorStream.close();
            return new String(secretStream.toByteArray(), Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getKeyID(PGPSecretKey secretKey) {
        return Long.toString(secretKey.getKeyID());
    }

    private static PGPSecretKeyRingCollection extractSecretKeyCollection(byte[] privateKey) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(privateKey);
        try {
            return new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(inputStream),
                    new JcaKeyFingerprintCalculator());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PGPException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static PGPSecretKey getSecretKey(String keyID, byte[] privateKey) {
        PGPSecretKeyRingCollection keyRing = extractSecretKeyCollection(privateKey);
        try {
            return keyRing.getSecretKey(new Long(keyID));
        } catch (PGPException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean testPassphraseForKey(String keyID, byte[] privateKey, String passphrase) {
        boolean unlocked = false;

        PGPSecretKey secretKey = getSecretKey(keyID, privateKey);
        if (secretKey == null) {
            // TODO: raise exception to show a different warning then "invalid passphrase"
        } else {
            try {
                PBESecretKeyDecryptor decrypterFactory = new JcePBESecretKeyDecryptorBuilder()
                        .setProvider("SC")
                        .build(passphrase.toCharArray());
                secretKey.extractPrivateKey(decrypterFactory);
                unlocked = true;
            } catch(PGPException e){
                e.printStackTrace();
            }
        }

        return unlocked;
    }


//    /**
//     * Constructor.
//     * @param context
//     */
//    public PgpHelper(Context context) {
//        mContext = context;
//        mStorageHelper = new StorageHelper(mContext);
//    }
//
//    /**
//     * Function to decrypt a encrypted string with the given PrivateKey.
//     * @param encryptedData
//     * @param privateKey
//     * @return
//     */
//    public String decrypt(String encryptedData, PGPPrivateKey privateKey) {
//        try {
//            // Convert the String to a InputStream.
//            byte[] encryptedBytes = encryptedData.getBytes(Charset.forName("UTF-8"));
//            InputStream encryptedInputStream = new ByteArrayInputStream(encryptedBytes);
//            encryptedInputStream = PGPUtil.getDecoderStream(encryptedInputStream);
//            // Create PGP Objects from the InputStream.
//            PGPObjectFactory pgpObjectFactory = new JcaPGPObjectFactory(encryptedInputStream);
//
//            // Define used objects.
//            PGPEncryptedDataList encryptedDataList;
//            PGPPublicKeyEncryptedData publicKeyEncryptedData = null;
//
//            // Get first PGP Object.
//            Object pgpObject = pgpObjectFactory.nextObject();
//
//            // Make sure we have a PGPEncryptedDataList.
//            if (pgpObject instanceof PGPEncryptedDataList) {
//                encryptedDataList = (PGPEncryptedDataList) pgpObject;
//            } else {
//                encryptedDataList = (PGPEncryptedDataList) pgpObjectFactory.nextObject();
//            }
//
//            // Create iterator for all EncryptedDataObjects. A PGP Message can have multiple
//            // EncryptedDataObjects with the same content only encrypted with different PublicKeys.
//            Iterator encryptedDataIterator = encryptedDataList.getEncryptedDataObjects();
//
//            boolean hasPrivateKeyMatch = false;
//
//            // Loop all EncryptedDataObjects.
//            while (encryptedDataIterator.hasNext()) {
//                // Get the EncryptedData.
//                publicKeyEncryptedData = (PGPPublicKeyEncryptedData) encryptedDataIterator.next();
//                // Get the ID of the key that needs to be used for decrypting the data.
//                long keyId = publicKeyEncryptedData.getKeyID();
//
//                if (privateKey.getKeyID() == keyId) {
//                    hasPrivateKeyMatch = true;
//                    break;
//                }
//            }
//
//            // No PrivateKey has been found to decrypt this message.
//            if (!hasPrivateKeyMatch) {
//                return "";
//            }
//
//            // Decrypt data with PrivateKey.
//            InputStream decryptedData = publicKeyEncryptedData.getDataStream(new BcPublicKeyDataDecryptorFactory(privateKey));
//
//            // Create PGPObjects for this decrypted data.
//            pgpObjectFactory = new JcaPGPObjectFactory(decryptedData);
//
//            // Get the decrypted message.
//            Object decryptedMessage = pgpObjectFactory.nextObject();
//
//            // Decompress the message if it is compressed.
//            if (decryptedMessage instanceof PGPCompressedData) {
//                PGPCompressedData compressedData = (PGPCompressedData) decryptedMessage;
//                pgpObjectFactory = new JcaPGPObjectFactory(compressedData.getDataStream());
//
//                decryptedMessage = pgpObjectFactory.nextObject();
//            }
//
//            // Convert the LiteralData steam into a String object.
//            if (decryptedMessage instanceof PGPLiteralData) {
//                PGPLiteralData literalData = (PGPLiteralData) decryptedMessage;
//                return new String(Streams.readAll(literalData.getInputStream()), Charset.forName("UTF-8"));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "";
//    }
//
//    /**
//     * Function to generate a KeyPair.
//     * @param name Name of the KeyPair.
//     * @param password Password for encrypting the PrivateKey.
//     */
//    public void generateKeyPair(String name, String password) {
//        try {
//            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "SC");
//            generator.initialize(KEY_PAIR_BITS);
//            KeyPair pair = generator.generateKeyPair();
//
//            PGPDigestCalculator sha1Calc = new JcaPGPDigestCalculatorProviderBuilder().build().get(HashAlgorithmTags.SHA1);
//            PGPKeyPair pgpPair = new JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, pair, new Date());
//
//            PGPSecretKey secretKey = new PGPSecretKey(
//                    PGPSignature.DEFAULT_CERTIFICATION,
//                    pgpPair,
//                    name,
//                    sha1Calc,
//                    null,
//                    null,
//                    new JcaPGPContentSignerBuilder(
//                            pgpPair.getArmoredPublicKey().getAlgorithm(),
//                            HashAlgorithmTags.SHA1),
//                    new JcePBESecretKeyEncryptorBuilder(
//                            PGPEncryptedData.CAST5, sha1Calc)
//                            .setProvider("SC")
//                            .build(password.toCharArray()));
//
//            // Convert PGPPublicKey to armored string.
//            ByteArrayOutputStream pubStream = new ByteArrayOutputStream();
//            ArmoredOutputStream pubArmorStream = new ArmoredOutputStream(pubStream);
//            secretKey.getArmoredPublicKey().encode(pubArmorStream);
//            pubArmorStream.close();
//            String publicKeyString = new String(pubStream.toByteArray(), Charset.forName("UTF-8"));
//
//            // Convert PGPSecretKey to armored string.
//            ByteArrayOutputStream secretStream = new ByteArrayOutputStream();
//            ArmoredOutputStream secretArmorStream = new ArmoredOutputStream(secretStream);
//            secretKey.encode(secretArmorStream);
//            secretArmorStream.close();
//            String secretKeyString = new String(secretStream.toByteArray(), Charset.forName("UTF-8"));
//
//            mStorageHelper.setPublicKeyName(name);
//            mStorageHelper.setPublicKey(publicKeyString);
//            mStorageHelper.setSecretKey(secretKeyString);
//            mStorageHelper.setSecretKeyId(secretKey.getKeyID());
//
//            resetCachedValues();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Function to reset the cached values.
//     */
//    private void resetCachedValues() {
//        mSecretKeyCollection = null;
//    }
//
//    /**
//     * Function to get the SecretKeyCollection from the armored string of the secret key in
//     * the storage.
//     * @return
//     */
//    private PGPSecretKeyRingCollection getSecretKeyCollection() {
//        if (mSecretKeyCollection == null) {
//            try {
//                String privateKeyArmored = mStorageHelper.getSecretKey();
//                InputStream inputStream = new ByteArrayInputStream(
//                        privateKeyArmored.getBytes(Charset.forName("UTF-8")));
//                inputStream = PGPUtil.getDecoderStream(inputStream);
//
//                mSecretKeyCollection = new PGPSecretKeyRingCollection(
//                        inputStream, new JcaKeyFingerprintCalculator());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        return mSecretKeyCollection;
//    }
//
//    /**
//     * Function to get the secret key id from storage.
//     * @return
//     */
//    private long getSecretKeyId() {
//        if (mSecretKeyId == -1) {
//            mSecretKeyId = mStorageHelper.getSecretKeyId();
//        }
//        return mSecretKeyId;
//    }
//
//    /**
//     * Function to get a PrivateKey for the given password.
//     * @param password
//     * @return
//     */
//    public PGPPrivateKey getArmoredPrivateKey(String password) {
//        return getArmoredPrivateKey(getSecretKeyId(), password);
//    }
//
//    /**
//     * Function to get the private key used for decrypting
//     * @param keyId The key that is needed.
//     * @param password Password to decrypt the SecretKey into a PrivateKey user for decrypting
//     *                 messages.
//     * @return
//     */
//    private PGPPrivateKey getArmoredPrivateKey(long keyId, String password) {
//        try {
//            PGPSecretKey secretKey = getSecretKeyCollection().getSecretKey(keyId);
//
//            if (secretKey != null) {
//                return secretKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider("SC").build(password.toCharArray()));
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
}