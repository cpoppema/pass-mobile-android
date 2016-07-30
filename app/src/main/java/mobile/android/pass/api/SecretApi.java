package mobile.android.pass.api;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Api class for getting a specific secret.
 */
public class SecretApi extends Api implements Response.Listener<JSONObject>, Response.ErrorListener {
    private static final String BODY_PATH = "path";
    private static final String BODY_USERNAME = "username";

    private SecretCallback mCallback;

    public SecretApi(Context context, SecretCallback callback) {
        super(context);
        mCallback = callback;
    }

    public void getSecret(String path, String username) {
        JSONObject body = getDefaultJsonBody();
        try {
            body.put(BODY_PATH, path);
            body.put(BODY_USERNAME, username);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                mStorageHelper.getServerAddress() + SECRET_ENDPOINT,
                body,
                this,  // Response.Listener
                this  // Response.ErrorListener
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        mRequestQueue.add(request);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mCallback.onSecretApiFailure(getVolleyErrorFeedbackText(error));
    }

    @Override
    public void onResponse(JSONObject response) {
        try {
            String pgpMessage = response.getString(mResponseKey);
            mCallback.onSecretApiResponse(pgpMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
