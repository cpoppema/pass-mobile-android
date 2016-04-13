package mobile.android.pass;

import org.spongycastle.openpgp.PGPPrivateKey;

/**
 * Created by marco on 13/04/16.
 */
public interface PasswordCallback {
    void onCorrectPassword(PGPPrivateKey privateKey);
    void onIncorrectPassword();
}
