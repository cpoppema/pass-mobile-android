package mobile.android.pass.api;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import mobile.android.pass.R;
import mobile.android.pass.utils.StorageHelper;

/**
 * Superclass with basic Api functions.
 */
public abstract class Api {
    public static final String DEFAULT_KEY = "publicKey";
    public static final String SECRETS_ENDPOINT = "/secrets/";
    public static final String SECRET_ENDPOINT = "/secret/";

    private static final String TAG = Api.class.toString();

    private Context mContext;

    protected final String mResponseKey = "response";
    protected RequestQueue mRequestQueue;
    protected StorageHelper mStorageHelper;
    protected String mTag;


    public Api(Context context) {
        mContext = context;
        mStorageHelper = new StorageHelper(mContext);
        mRequestQueue = Volley.newRequestQueue(mContext);
    }

    protected JSONObject getDefaultJsonBody() {
        try {
            return new JSONObject().put(DEFAULT_KEY, mStorageHelper.getArmoredPublicKey());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected String getVolleyErrorFeedbackText(VolleyError error) {
        if (error instanceof TimeoutError) {
            return mContext.getString(R.string.volley_timeout_error_message);
        } else if (error instanceof NoConnectionError) {
            return mContext.getString(R.string.volley_no_connection_error_message);
        } else if (error instanceof AuthFailureError) {
            return mContext.getString(R.string.volley_auth_failure_error_message);
        } else if (error instanceof ServerError) {
            return mContext.getString(R.string.volley_server_error_message);
        } else if (error instanceof NetworkError) {
            return mContext.getString(R.string.volley_network_error_message);
        } else if (error instanceof ParseError) {
            return mContext.getString(R.string.volley_parse_error_message);
        }
        return mContext.getString(R.string.volley_generic_error_message);
    }

    public void cancelAll() {
        Log.d(TAG, "Canceling ongoing/queue requests.");
        mRequestQueue.cancelAll(mTag);
    }
}
