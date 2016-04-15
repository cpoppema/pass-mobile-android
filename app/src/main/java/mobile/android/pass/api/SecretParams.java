package mobile.android.pass.api;

import com.google.gson.annotations.SerializedName;

/**
 * Parameter class for the secret api call.
 */
public class SecretParams extends BaseParams {

    @SerializedName("username")
    public final String mUsername;
    @SerializedName("path")
    public final String mPath;

    public SecretParams(String publicKey, String userName, String path) {
        super(publicKey);
        mUsername = userName;
        mPath = path;

    }
}
