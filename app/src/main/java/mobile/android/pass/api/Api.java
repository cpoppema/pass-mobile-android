package mobile.android.pass.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import mobile.android.pass.utils.StorageHelper;

/**
 * Created by marco on 5/27/16.
 */
public class Api {
    public final static String SECRETS_ENDPOINT = "/secrets/";
    public final static String SECRET_ENDPOINT = "/secret/";

    private Context mContext;
    protected RequestQueue mRequestQueue;
    protected StorageHelper mStorageHelper;


    public Api(Context context) {
        mContext = context;
        mStorageHelper = new StorageHelper(mContext);
        mRequestQueue = Volley.newRequestQueue(mContext);
    }

    protected JSONObject getDefaultJsonBody() {
        try {
            return new JSONObject().put("publicKey", mStorageHelper.getArmoredPublicKey());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


//    @Override
//    public void onErrorResponse(VolleyError error) {
//        Log.d("HELLO", "failure");
////        mCallback.onApiFailure();
//    }
//
//    @Override
//    public void onResponse(JSONObject response) {
//        Log.d("HELLO", response.toString());
//        try {
//            String pgpMessage = response.getString("response");
//            mCallback.onApiResponse(pgpMessage);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
}
