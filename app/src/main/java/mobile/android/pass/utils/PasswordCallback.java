package mobile.android.pass.utils;

import org.spongycastle.openpgp.PGPPrivateKey;

/**
 * Interface that holds callback functions for the password dialog.
 */
public interface PasswordCallback {
    void onCorrectPassword(PGPPrivateKey privateKey);
    void onIncorrectPassword();
}
