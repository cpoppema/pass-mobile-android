package mobile.android.pass.api;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import org.spongycastle.openpgp.PGPPrivateKey;

import mobile.android.pass.pgp.PgpHelper;

/**
 * Class that stores the encrypted data of the api response and provides a decrypt option.
 */
public class ApiResponse {

    @SerializedName("response")
    private String mEncryptedResponseData;

    public String decryptResponseData(Context context, PGPPrivateKey privateKey) {
        PgpHelper pgpHelper = new PgpHelper(context);

        return pgpHelper.decrypt(mEncryptedResponseData, privateKey);
    }
}
