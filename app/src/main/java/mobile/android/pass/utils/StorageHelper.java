package mobile.android.pass.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import org.spongycastle.openpgp.PGPSecretKey;

import java.nio.charset.Charset;

/**
 * Class that acts as middleware between a storage engine and the app.
 */
public class StorageHelper implements StorageKeys {
    private Context mContext;
    private SharedPreferences mSharedPreferences;

    public enum StorageKey {
        PUBLIC_KEY_LEGACY("public"),
        PUBLIC_KEY_NAME_LEGACY("public_name"),
        SECRET_KEY_LEGACY("secret"),
        SECRET_KEY_ID_LEGACY("secret_id"),
        SERVER_ADDRESS_LEGACY("server"),

        PRIVATE_KEY("prey_key_private_key"),
        PUBLIC_KEY("prey_key_public_key"),
        PUBLIC_KEY_NAME("pref_key_key_name"),
        PUBLIC_KEY_ID("pref_key_public_key_id"),
        SERVER_ADDRESS("pref_key_server");

        private String key;

        StorageKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        @Override
        public String toString() {
            return this.getKey();
        }
    }

    /**
     * Constructor.
     * @param context
     */
    public StorageHelper(Context context) {
        mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        this.convertFromLegacy();
    }

    private void convertFromLegacy() {
        // Get legacy values.
        String publicKey = this.getString(StorageKey.PUBLIC_KEY_LEGACY);
        String publicKeyName = this.getString(StorageKey.PUBLIC_KEY_NAME_LEGACY);
        String secretKey = this.getString(StorageKey.SECRET_KEY_LEGACY);
        String secretKeyID = this.getString(StorageKey.SECRET_KEY_ID_LEGACY);
        String serverAddress = this.getString(StorageKey.SERVER_ADDRESS_LEGACY);

        // Overwrite and remove non-empty legacy values.
        if (!TextUtils.isEmpty(publicKey)) {
            this.putString(StorageKey.PUBLIC_KEY, publicKey);
            this.remove(StorageKey.PUBLIC_KEY_LEGACY);
        }
        if (!TextUtils.isEmpty(publicKey)) {
            this.putString(StorageKey.PUBLIC_KEY_NAME, publicKeyName);
            this.remove(StorageKey.PUBLIC_KEY_NAME_LEGACY);
        }
        if (!TextUtils.isEmpty(publicKey)) {
            this.putString(StorageKey.PRIVATE_KEY, secretKey);
            this.remove(StorageKey.SECRET_KEY_LEGACY);
        }
        if (!TextUtils.isEmpty(publicKey)) {
            this.putString(StorageKey.PUBLIC_KEY_ID, secretKeyID);
            this.remove(StorageKey.SECRET_KEY_ID_LEGACY);
        }
        if (!TextUtils.isEmpty(publicKey)) {
            this.putString(StorageKey.SERVER_ADDRESS, serverAddress);
            this.remove(StorageKey.SERVER_ADDRESS_LEGACY);
        }
    }

    private void remove(StorageKey key) {
        mSharedPreferences.edit().remove(key.toString());
    }

    private String getString(StorageKey key) {
        return mSharedPreferences.getString(key.toString(), "");
    }

    public void putString(StorageKey key, String value) {
        mSharedPreferences.edit().putString(key.toString(), value).apply();
    }

    public String getKeyName() {
        return this.getString(StorageKey.PUBLIC_KEY_NAME);
    }

    public void putKeyName(String keyName) {
        this.putString(StorageKey.PUBLIC_KEY_NAME, keyName);
    }

    public void putKeyPair(PGPSecretKey keyPair) {
        String publicKey = PgpHelper.extractArmoredPublicKey(keyPair);
        String privateKey = PgpHelper.extractArmoredPrivateKey(keyPair);
        String keyID = PgpHelper.getKeyID(keyPair);

        this.putArmoredPublicKey(publicKey);
        this.putArmoredPrivateKey(privateKey);
        this.putKeyID(keyID);
    }

    public void putArmoredPublicKey(String publicKey) {
        this.putString(StorageKey.PUBLIC_KEY, publicKey);
    }

    public String getArmoredPublicKey() {
        return this.getString(StorageKey.PUBLIC_KEY);
    }

    public void putArmoredPrivateKey(String privateKey) {
        this.putString(StorageKey.PRIVATE_KEY, privateKey);
    }

    public String getArmoredPrivateKey() {
        return this.getString(StorageKey.PRIVATE_KEY);
    }

    public byte[] getPrivateKey() {
        return this.getArmoredPrivateKey().getBytes(Charset.forName("UTF-8"));
    }

    public void putKeyID(String keyID) {
        this.putString(StorageKey.PUBLIC_KEY_ID, keyID);
    }

    public String getKeyID() {
        return this.getString(StorageKey.PUBLIC_KEY_ID);
    }


//    /**
//     * Function to put a string in the used storage engine.
//     * @param key
//     * @param value
//     */
//    private void putString(String key, String value) {
//        mSharedPreferences.edit().putString(key, value).apply();
//    }
//
//    /**
//     * Function to get a string from the storage for a given key.
//     * @param key
//     * @return
//     */
//    private String getString(String key) {
//       return mSharedPreferences.getString(key, "");
//    }
//
//    /**
//     * Function to put a long in the used storage engine.
//     * @param key
//     * @param value
//     */
//    private void putLong(String key, long value) {
//        mSharedPreferences.edit().putLong(key, value).apply();
//    }
//
//    /**
//     * Function to get a long from the storage for a given key.
//     * @param key
//     * @return
//     */
//    private long getLong(String key) {
//        return mSharedPreferences.getLong(key, 0);
//    }
//
//    /**
//     * Function to check if a keypair is present in the storage.
//     * @return
//     */
//    public boolean hasKeyPair() {
//        return mSharedPreferences.contains(PUBLIC_KEY) && mSharedPreferences.contains(SECRET_KEY);
//    }
//
//    public void setPublicKey(String publicKey) {
//        putString(PUBLIC_KEY, publicKey);
//    }
//
//    public String getArmoredPublicKey() {
//        return getString(PUBLIC_KEY);
//    }
//
//    public void setPublicKeyName(String publicKeyName) {
//        putString(PUBLIC_KEY_NAME, publicKeyName);
//    }
//
//    public String getPublicKeyName() {
//        return getString(PUBLIC_KEY_NAME);
//    }
//
//    public void setSecretKey(String secretKey) {
//        putString(SECRET_KEY, secretKey);
//    }
//
//    public String getSecretKey() {
//        return getString(SECRET_KEY);
//    }
//
//    public void setSecretKeyId(long secretKeyId) {
//        putLong(SECRET_KEY_ID, secretKeyId);
//    }
//
//    public long getSecretKeyId() {
//        return getLong(SECRET_KEY_ID);
//    }
//
//    public void setServerAddress(String serverAddress) {
//        putString(SERVER_ADDRESS, serverAddress);
//    }
//
//    public String getServerAddress() {
//        return getString(SERVER_ADDRESS);
//    }

}
