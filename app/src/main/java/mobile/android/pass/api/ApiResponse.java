package mobile.android.pass.api;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import mobile.android.pass.pgp.PgpHelper;

/**
 * Created by marcov on 13-4-16.
 */
public class ApiResponse {

    @SerializedName("response")
    private String mEncryptedResponseData;

    public String decryptResponseData(Context context, String password) {
        PgpHelper pgpHelper = new PgpHelper(context);

        return pgpHelper.decrypt(mEncryptedResponseData, password);
    }
}
