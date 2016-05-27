package mobile.android.pass.api;

/**
 * Created by marco on 7/8/16.
 */
public interface SecretCallback {
    void onSecretApiResponse(String pgpResponse);
    void onSecretApiFailure();
}
