package mobile.android.pass.api;

/**
 * Created by marco on 7/8/16.
 */
public interface SecretsCallback {
    void onSecretsApiResponse(String pgpResponse);
    void onSecretsApiFailure();
}
