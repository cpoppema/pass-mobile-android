package mobile.android.pass.api;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import org.spongycastle.openpgp.PGPPrivateKey;

import mobile.android.pass.utils.PgpHelper;

/**
 * Base reponse object for api requests that return encrypted data.
 */
public class ApiResponse {

    @SerializedName("response")
    private String mEncryptedResponseData;

//    public String decryptResponseData(Context context, PGPPrivateKey privateKey) {
//        PgpHelper pgpHelper = new PgpHelper(context);
//
//        return pgpHelper.decrypt(mEncryptedResponseData, privateKey);
//    }
}
