package mobile.android.pass.settings;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.spongycastle.openpgp.PGPSecretKey;

import mobile.android.pass.utils.PgpHelper;

public class CreateKeyTaskLoader extends AsyncTaskLoader<PGPSecretKey> {

    private static final String TAG = CreateKeyTaskLoader.class.toString();

    private final String mKeyName;
    private final String mPassword;
    private PGPSecretKey mKeyPair;
    private boolean isStopped;

    public CreateKeyTaskLoader(Context context, String keyName, String passphrase) {
        super(context);

        mKeyName = keyName;
        mPassword = passphrase;
        isStopped = false;
    }

    @Override
    public PGPSecretKey loadInBackground() {
        Log.d(TAG, "loadInBackground");
        try {
            Log.d(TAG, "sleeping");
            // Simulate creating a strong keypair.
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            return null;
        }

        mKeyPair = PgpHelper.generateKeyPair(mKeyName, mPassword);
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

        if(isStarted()) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        Log.d(TAG, "onStopLoading");
        super.onStopLoading();
        isStopped = true;
    }
}