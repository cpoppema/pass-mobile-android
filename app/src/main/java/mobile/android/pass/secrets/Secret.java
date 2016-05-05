package mobile.android.pass.secrets;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Secret {
    private String mDomain;
    private String mPath;
    private String mUsername;
    private String mUsernameNormalized;

    static final String DOMAIN = "domain";
    static final String PATH = "path";
    static final String USERNAME = "username";
    static final String USERNAME_NORMALIZED = "username_normalized";

    public Secret(String domain, String path, String username, String usernameNormalized) {
        this.mDomain = domain;
        this.mPath = path;
        this.mUsername = username;
        this.mUsernameNormalized = usernameNormalized;
    }

    public Secret(JSONObject object){
        try {
            this.mDomain = object.getString("domain");
            this.mPath = object.getString("path");
            this.mUsername = object.getString("username");
            this.mUsernameNormalized = object.getString("username_normalized");
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
