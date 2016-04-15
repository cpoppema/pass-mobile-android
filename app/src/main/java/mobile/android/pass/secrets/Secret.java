package mobile.android.pass.secrets;

import com.google.gson.annotations.SerializedName;

/**
 * Class used in GSON parse to convert JSON data to this object.
 */
public class Secret {

    @SerializedName("domain")
    private String mDomain;
    @SerializedName("path")
    private String mPath;
    @SerializedName("username")
    private String mUsername;
    @SerializedName("username_normalized")
    private String mUsernameNormalized;

    public String getDomain() {
        return mDomain;
    }

    public void setDomain(String domain) {
        this.mDomain = domain;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String path) {
        this.mPath = path;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        this.mUsername = username;
    }

    public String getUsernameNormalized() {
        return mUsernameNormalized;
    }

    public void setUsernameNormalized(String usernameNormalized) {
        this.mUsernameNormalized = usernameNormalized;
    }
}
