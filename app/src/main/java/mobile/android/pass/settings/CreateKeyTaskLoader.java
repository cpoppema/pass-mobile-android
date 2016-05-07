package mobile.android.pass.settings;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import mobile.android.pass.secrets.SecretsActivity;

public class CreateKeyTaskLoader extends AsyncTaskLoader<Object> {

    private static final String TAG = SecretsActivity.class.toString();

    private final String mKeyName;
    private final String mPassword;
    private Object mKeyPair;

    public CreateKeyTaskLoader(Context context, String keyName, String passphrase) {
        super(context);

        mKeyName = keyName;
        mPassword = passphrase;
    }

    @Override
    public Object loadInBackground() {
        Log.d(TAG, "sleeping");
        try {
            // Simulate creating a strong keypair.
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            return null;
        }

        // TODO: create new keypair
        mKeyPair = new Object();
        return mKeyPair;
    }

    @Override
    public void deliverResult(Object keyPair) {
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