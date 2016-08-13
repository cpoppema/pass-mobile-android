package mobile.android.pass.secrets;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.ArrayList;

/**
 * Contains all the data to show it in the list of secrets and to uniquely identify it when
 * retrieving the password for it.
 */
public class Secret implements Parcelable {
    public static final Parcelable.Creator<Secret> CREATOR = new Parcelable.Creator<Secret>() {
        @Override
        public Secret createFromParcel(Parcel in) {
            return new Secret(in);
        }

        @Override
        public Secret[] newArray(int size) {
            return new Secret[size];
        }
    };
    public static final String DOMAIN = "domain";
    public static final String PATH = "path";
    public static final String USERNAME = "username";
    public static final String USERNAME_NORMALIZED = "username_normalized";

    private String mDomain;
    private String mPath;
    private String mUsername;
    private String mUsernameNormalized;

    // These are used as instance variables only and are not parcelable.
    private String mSecretText;
    private String mPassphrase;

    public Secret(JSONObject object) {
        try {
            mDomain = object.getString(DOMAIN);
            mPath = object.getString(PATH);
            mUsername = object.getString(USERNAME);
            mUsernameNormalized = object.getString(USERNAME_NORMALIZED);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Secret(Cursor cursor) {
        mDomain = cursor.getString(cursor.getColumnIndex(DOMAIN));
        mPath = cursor.getString(cursor.getColumnIndex(PATH));
        mUsername = cursor.getString(cursor.getColumnIndex(USERNAME));
        mUsernameNormalized = cursor.getString(cursor.getColumnIndex(USERNAME_NORMALIZED));
    }

    private Secret(Parcel in) {
        String[] data = new String[3];

        in.readStringArray(data);
        mDomain = data[0];
        mPath = data[1];
        mUsername = data[2];
        mUsernameNormalized = data[3];
    }

    // Factory method to convert an array of JSON objects into a list of objects
    // Secret.fromJson(jsonArray);
    public static ArrayList<Secret> fromJson(JSONArray jsonObjects) {
        ArrayList<Secret> secrets = new ArrayList<>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                secrets.add(new Secret(jsonObjects.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return secrets;
    }

    public String getDomain() {
        return mDomain;
    }

    public String getPath() {
        return mPath;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getUsernameNormalized() {
        return mUsernameNormalized;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{mDomain, mPath, mUsername, mUsernameNormalized});
    }

    /**
     * Returns true if @needle was found in @hay using fuzzy matching.
     */
    private boolean fuzzyContains(String hay, String needle) {
        hay = hay.toLowerCase();
        needle = needle.toLowerCase();

        int lastIndex = -1;
        for (int i = 0; i < needle.length(); i++) {
            String l = Character.toString(needle.charAt(i));
            if ((lastIndex = hay.indexOf(l, lastIndex + 1)) == -1) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns true if @needle is empty or when it is found in either the domain, username
     * (normalized) using fuzzy matching.
     **/
    public boolean isMatch(String needle) {
        return TextUtils.isEmpty(needle) || fuzzyContains(getDomain(), needle) ||
                fuzzyContains(getUsername(), needle) || fuzzyContains(getUsernameNormalized(), needle);
    }

    public String getSecretText() {
        return mSecretText;
    }

    /**
     * Set the full contents of a secret, doing this allows for setting the passphrase "parsing"
     * in this class in a centralized place.
     */
    public void setSecretText(String secretText) {
        mSecretText = secretText;
    }

    public String getPassphrase() {
        if (mPassphrase == null) {
            // Read the first line as the password.
            mPassphrase = getSecretText().split("\n")[0];
        }
        return mPassphrase;
    }
}
