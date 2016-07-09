package mobile.android.pass.api;

/**
 * Callback interface to pass secret api response to calling Activity.
 */
public interface SecretCallback {
    void onSecretApiResponse(String pgpResponse);
    void onSecretApiFailure(String errorMessage);
}
