package mobile.android.pass;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by marcov on 14-4-16.
 */
public class Storage implements StorageKeys {
    private Context mContext;
    private SharedPreferences mSharedPreferences;

    public Storage(Context context) {
        mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    private void putString(String key, String value) {
        mSharedPreferences.edit().putString(key, value).apply();
    }

    private String getString(String key) {
       return mSharedPreferences.getString(key, "");
    }

    private void putLong(String key, long value) {
        mSharedPreferences.edit().putLong(key, value).apply();
    }

    private long getLong(String key) {
        return mSharedPreferences.getLong(key, 0);
    }

    
    public void setPublicKey(String publicKey) {
        putString(PUBLIC_KEY, publicKey);
    }

    public String getPublicKey() {
        return getString(PUBLIC_KEY);
    }

}
