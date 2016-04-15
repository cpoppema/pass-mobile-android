package mobile.android.pass.api;

import com.google.gson.annotations.SerializedName;

/**
 * Params class for the basics required in every request.
 */
public class BaseParams {

    @SerializedName("publicKey")
    public final String mPublicKey;

    public BaseParams(String publicKey) {
        mPublicKey = publicKey;
    }
}
