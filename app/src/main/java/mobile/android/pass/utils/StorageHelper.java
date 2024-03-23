package mobile.android.pass.utils;

import org.spongycastle.openpgp.PGPSecretKey;

import android.content.Context;
import android.content.SharedPreferences;

import java.nio.charset.StandardCharsets;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

/**
 * Class that acts as middleware between a storage engine and the app.
 */
public class StorageHelper {
    private final SharedPreferences mSharedPreferences;

    /**
     * Constructor.
     */
    public StorageHelper(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
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
        return this.getArmoredPrivateKey().getBytes(StandardCharsets.UTF_8);
    }

    public void putKeyID(String keyID) {
        this.putString(StorageKey.PUBLIC_KEY_ID, keyID);
    }

    public String getKeyID() {
        return this.getString(StorageKey.PUBLIC_KEY_ID);
    }

    public String getServerAddress() {
        return this.getString(StorageKey.SERVER_ADDRESS);
    }

    public void putServerAddress(String serverAddress) {
        this.putString(StorageKey.SERVER_ADDRESS, serverAddress);
    }

    public enum StorageKey {
        PRIVATE_KEY("prey_key_private_key"),
        PUBLIC_KEY("prey_key_public_key"),
        PUBLIC_KEY_NAME("pref_key_key_name"),
        PUBLIC_KEY_ID("pref_key_public_key_id"),
        SERVER_ADDRESS("pref_key_server");

        private final String key;

        StorageKey(String key) {
            this.key = key;
        }

        @NonNull
        @Override
        public String toString() {
            return this.key;
        }
    }
}
