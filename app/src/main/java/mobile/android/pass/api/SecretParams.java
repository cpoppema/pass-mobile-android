package mobile.android.pass.api;

import com.google.gson.annotations.SerializedName;

import org.spongycastle.jcajce.provider.symmetric.ARC4;

/**
 * Created by marcov on 13-4-16.
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
