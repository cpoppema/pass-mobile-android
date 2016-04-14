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
 * Created by marco on 13/04/16.
 */
public class SecretsResponse extends ApiResponse {

    private List<Secret> convertJsonToSecretList(String secretsJson) {
        Type listType = new TypeToken<ArrayList<Secret>>() {}.getType();
        return new Gson().fromJson(secretsJson, listType);
    }

    public List<Secret> getSecrets(Context context, PGPPrivateKey privateKey) {
        String secretsJson = decryptResponseData(context, privateKey);

        return convertJsonToSecretList(secretsJson);
    }
}
