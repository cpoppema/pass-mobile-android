package mobile.android.pass.api;

/**
 * Callback interface to pass secret api response to calling Activity.
 */
public interface SecretCallback {
    void onSecretApiSuccess(String pgpResponse);

    void onSecretApiFailure(String errorMessage);
}
