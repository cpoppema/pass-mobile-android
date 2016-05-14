package mobile.android.pass.api;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.spongycastle.openpgp.PGPPrivateKey;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import mobile.android.pass.secrets.Secret;

/**
 * Response class for the secrets endpoint.
 */
public class SecretsResponse extends ApiResponse {

    /**
     * Function to convert Json to a list of objects.
     * @param secretsJson
     * @return
     */
    private List<Secret> convertJsonToSecretList(String secretsJson) {
        Type listType = new TypeToken<ArrayList<Secret>>() {}.getType();
        return new Gson().fromJson(secretsJson, listType);
    }

    /**
     * Function to return the response in a list of Secret objects.
     * @param context
     * @param privateKey
     * @return
     */
//    public List<Secret> getSecrets(Context context, PGPPrivateKey privateKey) {
//        String secretsJson = decryptResponseData(context, privateKey);
//
//        return convertJsonToSecretList(secretsJson);
//    }
}
