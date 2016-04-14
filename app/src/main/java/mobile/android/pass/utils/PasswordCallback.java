package mobile.android.pass.utils;

import org.spongycastle.openpgp.PGPPrivateKey;

/**
 * Created by marco on 13/04/16.
 */
public interface PasswordCallback {
    void onCorrectPassword(PGPPrivateKey privateKey);
    void onIncorrectPassword();
}
