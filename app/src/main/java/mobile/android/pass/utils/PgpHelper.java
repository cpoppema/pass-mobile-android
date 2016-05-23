package mobile.android.pass.utils;

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
import org.spongycastle.openpgp.PGPPrivateKey;
import org.spongycastle.openpgp.PGPPublicKey;
import org.spongycastle.openpgp.PGPPublicKeyEncryptedData;
import org.spongycastle.openpgp.PGPSecretKey;
import org.spongycastle.openpgp.PGPSecretKeyRing;
import org.spongycastle.openpgp.PGPSecretKeyRingCollection;
import org.spongycastle.openpgp.PGPSignature;
import org.spongycastle.openpgp.PGPUtil;
import org.spongycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.spongycastle.openpgp.operator.PBESecretKeyDecryptor;
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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.Date;
import java.util.Iterator;

/**
 * Class for generating PGP key pairs and decrypting data.
 */
public class PgpHelper {

    private static final String TAG = PgpHelper.class.toString();

    private static final int KEY_PAIR_BITS = 2048;

    // Make sure BouncyCastle is set as security provider.
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Function to generate a KeyPair.
     *
     * @param name       Name of the KeyPair.
     * @param passphrase Passphrase required to unlock the key.
     */
    public static PGPSecretKey generateKeyPair(String name, String passphrase) {
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
                            .build(passphrase.toCharArray()));

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

    /**
     * Function to get the (hexadecimal) key id from the given key.
     */
    public static String getKeyID(PGPSecretKey secretKey) {
        return Long.toHexString(secretKey.getKeyID()).toUpperCase();
    }

    /**
     * Function to get the SecretKeyCollection from the private key.
     */
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

    private static PGPSecretKey getSecretKey(byte[] privateKey) {
        PGPSecretKeyRingCollection keyRing = extractSecretKeyCollection(privateKey);
        Iterator<PGPSecretKeyRing> keyRingIterator = keyRing.getKeyRings();
        PGPSecretKey secretKey = keyRingIterator.next().getSecretKey();
        return secretKey;
    }

    /**
     * Function to turn the private key into one that can be used for decrypting.
     *
     * @param passphrase Passphrase that unlocks the private key.
     */
    public static PGPPrivateKey extractPrivateKey(byte[] privateKey, String passphrase) {
        PGPSecretKey secretKey = getSecretKey(privateKey);
        try {
            PBESecretKeyDecryptor decrypterFactory = new JcePBESecretKeyDecryptorBuilder()
                    .setProvider("SC")
                    .build(passphrase.toCharArray());
            return secretKey.extractPrivateKey(decrypterFactory);
        } catch (PGPException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean testPassphraseForKey(byte[] privateKey, String passphrase) {
        boolean unlocked = extractPrivateKey(privateKey, passphrase) != null;
        return unlocked;
    }

    /**
     * Function to decrypt a encrypted string with the given PrivateKey.
     *
     * @param ciphertext Encrypted string.
     */
    public static String decrypt(PGPPrivateKey privateKey, String ciphertext) {
        try {
            // Convert the String to a InputStream.
            byte[] encryptedBytes = ciphertext.getBytes(Charset.forName("UTF-8"));
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
                return null;
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

        return null;
    }
}