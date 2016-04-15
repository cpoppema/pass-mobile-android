package mobile.android.pass.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Class that acts as middleware between a storage engine and the app.
 */
public class Storage implements StorageKeys {
    private Context mContext;
    private SharedPreferences mSharedPreferences;

    /**
     * Constructor.
     * @param context
     */
    public Storage(Context context) {
        mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    /**
     * Function to put a string in the used storage engine.
     * @param key
     * @param value
     */
    private void putString(String key, String value) {
        mSharedPreferences.edit().putString(key, value).apply();
    }

    /**
     * Function to get a string from the storage for a given key.
     * @param key
     * @return
     */
    private String getString(String key) {
       return mSharedPreferences.getString(key, "");
    }

    /**
     * Function to put a long in the used storage engine.
     * @param key
     * @param value
     */
    private void putLong(String key, long value) {
        mSharedPreferences.edit().putLong(key, value).apply();
    }

    /**
     * Function to get a long from the storage for a given key.
     * @param key
     * @return
     */
    private long getLong(String key) {
        return mSharedPreferences.getLong(key, 0);
    }

    /**
     * Function to check if a keypair is present in the storage.
     * @return
     */
    public boolean hasKeyPair() {
        return mSharedPreferences.contains(PUBLIC_KEY) && mSharedPreferences.contains(SECRET_KEY);
    }

    public void setPublicKey(String publicKey) {
        putString(PUBLIC_KEY, publicKey);
    }

    public String getPublicKey() {
        return getString(PUBLIC_KEY);
    }

    public void setPublicKeyName(String publicKeyName) {
        putString(PUBLIC_KEY_NAME, publicKeyName);
    }

    public String getPublicKeyName() {
        return getString(PUBLIC_KEY_NAME);
    }

    public void setSecretKey(String secretKey) {
        putString(SECRET_KEY, secretKey);
    }

    public String getSecretKey() {
        return getString(SECRET_KEY);
    }

    public void setSecretKeyId(long secretKeyId) {
        putLong(SECRET_KEY_ID, secretKeyId);
    }

    public long getSecretKeyId() {
        return getLong(SECRET_KEY_ID);
    }

    public void setServerAddress(String serverAddress) {
        putString(SERVER_ADDRESS, serverAddress);
    }

    public String getServerAddress() {
        return getString(SERVER_ADDRESS);
    }

}
