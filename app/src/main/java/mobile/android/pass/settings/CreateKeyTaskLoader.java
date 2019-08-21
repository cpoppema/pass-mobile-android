package mobile.android.pass.settings;

import org.spongycastle.openpgp.PGPSecretKey;

import android.content.Context;
import androidx.loader.content.AsyncTaskLoader;
import android.util.Log;

import mobile.android.pass.utils.PgpHelper;

/**
 * Creates a keypair using a key name and passphrase as input.
 */
public class CreateKeyTaskLoader extends AsyncTaskLoader<PGPSecretKey> {
    private static final String TAG = CreateKeyTaskLoader.class.toString();

    // Input key name.
    private final String mKeyName;
    // Input passphrase.
    private final String mPassphrase;

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

        // Generate keypair.
        PGPSecretKey keyPair = PgpHelper.generateKeyPair(mKeyName, mPassphrase);

        // When stopped, do not return a result.
        if (isStopped || isLoadInBackgroundCanceled()) {
            Log.d(TAG, "stopped");
            return null;
        }

        Log.d(TAG, "generated");
        return keyPair;
    }

    @Override
    public void deliverResult(PGPSecretKey keyPair) {
        Log.d(TAG, "deliverResult, result is null: " + Boolean.toString(keyPair == null));

        super.deliverResult(keyPair);
    }

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