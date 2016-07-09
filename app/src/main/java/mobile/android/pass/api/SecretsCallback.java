package mobile.android.pass.api;

import com.android.volley.VolleyError;

/**
 * Callback interface to pass secrets api response to calling Activity.
 */
public interface SecretsCallback {
    void onSecretsApiResponse(String pgpResponse);
    void onSecretsApiFailure(VolleyError error);
}
