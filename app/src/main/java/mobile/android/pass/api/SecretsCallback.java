package mobile.android.pass.api;

/**
 * Callback interface to pass secrets api response to calling Activity.
 */
public interface SecretsCallback {
    void onSecretsApiResponse(String pgpResponse);
    void onSecretsApiFailure(String errorMessage);
}
