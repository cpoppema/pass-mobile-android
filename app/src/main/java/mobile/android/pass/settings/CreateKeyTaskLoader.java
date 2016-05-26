package mobile.android.pass.settings;

import org.spongycastle.openpgp.PGPSecretKey;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import mobile.android.pass.utils.PgpHelper;

/** Creates a keypair using a key name and passphrase as input. **/

public class CreateKeyTaskLoader extends AsyncTaskLoader<PGPSecretKey> {
    private static final String TAG = CreateKeyTaskLoader.class.toString();

    // Input key name.
    private final String mKeyName;
    // Input passphrase.
    private final String mPassphrase;

    // The generated keypair.
    private PGPSecretKey mKeyPair;
    // Indicator whether or not this task was stopped.
    private boolean isStopped;

    public CreateKeyTaskLoader(Context context, String keyName, String passphrase) {
        super(context);

        mKeyName = keyName;
        mPassphrase = passphrase;
        isStopped = false;
    }

    @Override
    public PGPSecretKey loadInBackground() {
        Log.d(TAG, "loadInBackground");

        try {
            Log.d(TAG, "sleeping");
            // FIXME: Remove delay before creating a keypair to test cancellation.
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            return null;
        }

        // Generate keypair.
        mKeyPair = PgpHelper.generateKeyPair(mKeyName, mPassphrase);

        // When stopped, do not return a result.
        if (isStopped || isLoadInBackgroundCanceled()) {
            Log.d(TAG, "stopped");
            return null;
        }

        Log.d(TAG, "generated");
        return mKeyPair;
    }

    @Override
    public void deliverResult(PGPSecretKey keyPair) {
        Log.d(TAG, "deliverResult, result is null: " + Boolean.toString(keyPair == null));

        super.deliverResult(keyPair);
    }

//    @Override
//    protected boolean onCancelLoad() {
//        Log.d(TAG, "onCancelLoad");
//        return super.onCancelLoad();
//    }
//
//    @Override
//    protected void onForceLoad() {
//        Log.d(TAG, "onForceLoad");
//        super.onForceLoad();
//    }
//
//    @Override
//    protected void onReset() {
//        Log.d(TAG, "onReset");
//        super.onReset();
//    }

    @Override
    protected void onStartLoading() {
        Log.d(TAG, "onStartLoading");

        if (isStarted()) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        Log.d(TAG, "onStopLoading");

        super.onStopLoading();

        // Set flag to prevent this task from delivering output.
        isStopped = true;
    }
}