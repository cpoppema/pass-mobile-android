package mobile.android.pass;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Can be removed very soon.
 */
public class JsonHelper {

    public static List<Secret> convertJsonToSecretList(String secretsJson) {
        Type listType = new TypeToken<ArrayList<Secret>>() {}.getType();
        return new Gson().fromJson(secretsJson, listType);
    }
}
