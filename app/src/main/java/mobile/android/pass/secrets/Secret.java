package mobile.android.pass.secrets;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Secret {
    private String mDomain;
    private String mPath;
    private String mUsername;
    private String mUsernameNormalized;

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
