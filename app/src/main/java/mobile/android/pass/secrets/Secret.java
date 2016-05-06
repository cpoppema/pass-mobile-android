package mobile.android.pass.secrets;

import android.database.Cursor;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Secret {
    public static final String DOMAIN = "domain";
    public static final String PATH = "path";
    public static final String USERNAME = "username";
    public static final String USERNAME_NORMALIZED = "username_normalized";

    private String mDomain;
    private String mPath;
    private String mUsername;
    private String mUsernameNormalized;

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

    public boolean isMatch(String needle) {
        if(TextUtils.isEmpty(needle)) {
            return true;
        }
        return fuzzyContains(getDomain(), needle) || fuzzyContains(getUsername(), needle) || fuzzyContains(getUsernameNormalized(), needle);
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
}
