package mobile.android.pass.api;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import mobile.android.pass.pgp.PgpHelper;

/**
 * Class that stores the encrypted data of the api response and provides a decrypt option.
 */
public class ApiResponse {

    @SerializedName("response")
    private String mEncryptedResponseData;

    /**
     * Decrypt the encrypted data in the api response.
     * @param context
     * @param password Password to unlock the SecretKey.
     * @return
     */
    public String decryptResponseData(Context context, String password) {
        PgpHelper pgpHelper = new PgpHelper(context);

        return pgpHelper.decrypt(mEncryptedResponseData, password);
    }
}
