package mobile.android.pass.settings;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.spongycastle.openpgp.PGPSecretKey;

import mobile.android.pass.secrets.SecretsActivity;
import mobile.android.pass.utils.PgpHelper;

public class CreateKeyTaskLoader extends AsyncTaskLoader<PGPSecretKey> {

    private static final String TAG = SecretsActivity.class.toString();

    private final String mKeyName;
    private final String mPassword;
    private PGPSecretKey mKeyPair;

    public CreateKeyTaskLoader(Context context, String keyName, String passphrase) {
        super(context);

        mKeyName = keyName;
        mPassword = passphrase;
    }

    @Override
    public PGPSecretKey loadInBackground() {
        Log.d(TAG, "sleeping");
        try {
            // Simulate creating a strong keypair.
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            return null;
        }

        mKeyPair = PgpHelper.generateKeyPair(mKeyName, mPassword);
        return mKeyPair;
    }

    @Override
    public void deliverResult(PGPSecretKey keyPair) {
        super.deliverResult(keyPair);

        if (isReset()) {
            return;
        }
        mKeyPair = keyPair;

        if (isStarted()) {
            super.deliverResult(keyPair);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mKeyPair != null) {
            deliverResult(mKeyPair);
        }
        if (takeContentChanged() || mKeyPair == null) {
            forceLoad();
        }
    }
}