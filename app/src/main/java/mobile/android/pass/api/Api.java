package mobile.android.pass.api;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import mobile.android.pass.utils.StorageHelper;

/**
 * Superclass with basic Api functions.
 */
public abstract class Api {
    // Endpoints.
    public static final String SECRETS_ENDPOINT = "/secrets/";
    public static final String SECRET_ENDPOINT = "/secret/";

    public static final String DEFAULT_KEY = "publicKey";

    protected final String mResponseKey = "response";
    protected RequestQueue mRequestQueue;
    protected StorageHelper mStorageHelper;


    public Api(Context context) {
        mStorageHelper = new StorageHelper(context);
        mRequestQueue = Volley.newRequestQueue(context);
    }

    protected JSONObject getDefaultJsonBody() {
        try {
            return new JSONObject().put(DEFAULT_KEY, mStorageHelper.getArmoredPublicKey());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
