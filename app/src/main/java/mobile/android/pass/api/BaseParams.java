package mobile.android.pass.api;

import android.util.Base64;

import com.google.gson.annotations.SerializedName;

import java.security.PublicKey;

/**
 * Created by marcov on 13-4-16.
 */
public class BaseParams {

    @SerializedName("publicKey")
    public final String mPublicKey;

    public BaseParams(String publicKey) {
        mPublicKey = publicKey;
    }
}
