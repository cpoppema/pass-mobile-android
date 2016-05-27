package mobile.android.pass.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by marco on 7/8/16.
 */
public class SecretApi extends Api implements Response.Listener<JSONObject>, Response.ErrorListener {
    private SecretCallback mCallback;

    public SecretApi(Context context, SecretCallback callback) {
        super(context);
        mCallback = callback;
    }

    public void getSecret(String path, String username) {
        JSONObject body = getDefaultJsonBody();
        try {
            body.put("path", path);
            body.put("username", username);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                mStorageHelper.getServerAddress() + SECRET_ENDPOINT,
                body,
                this,  // Response.Listener
                this  // Response.ErrorListener
        );

        mRequestQueue.add(request);
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(JSONObject response) {
        try {
            String pgpMessage = response.getString("response");
            mCallback.onSecretApiResponse(pgpMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
