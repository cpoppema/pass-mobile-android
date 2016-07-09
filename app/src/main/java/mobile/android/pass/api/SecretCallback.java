package mobile.android.pass.api;

import com.android.volley.VolleyError;

/**
 * Callback interface to pass secret api response to calling Activity.
 */
public interface SecretCallback {
    void onSecretApiResponse(String pgpResponse);
    void onSecretApiFailure(VolleyError error);
}
