package mobile.android.pass.api;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

/**
 * Api class for getting a list of secrets.
 */
public class SecretsApi extends Api implements Response.Listener<JSONObject>, Response.ErrorListener {
    private final SecretsCallback mCallback;

    public SecretsApi(Context context, SecretsCallback callback) {
        super(context);
        mCallback = callback;
        mTag = SecretsApi.class.toString();
    }

    public void getSecrets() {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                mStorageHelper.getServerAddress() + SECRETS_ENDPOINT,
                getDefaultJsonBody(),
                this,  // Response.Listener
                this  // Response.ErrorListener
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        request.setTag(mTag);
        mRequestQueue.add(request);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mCallback.onSecretsApiFailure(getVolleyErrorFeedbackText(error));
    }

    @Override
    public void onResponse(JSONObject response) {
        try {
            String pgpMessage = response.getString(mResponseKey);
            mCallback.onSecretsApiSuccess(pgpMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
