package mobile.android.pass.secrets;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.BaseColumns;
import androidx.loader.content.AsyncTaskLoader;

import java.util.ArrayList;

/**
 * Loads a list of secrets into a @Cursor.
 */
public class SecretsTaskLoader extends AsyncTaskLoader<Cursor> {
    private static final String TAG = SecretsActivity.class.toString();

    // Data structure to store retrieved or filtered secrets in.
    private Cursor mCursor;
    // Query used to filter secrets.
    private String mFilter;
    // List of secrets before a filter is applied.
    private ArrayList<Secret> mOriginalSecrets;

    /** Constructor used when building a clean list of given secrets. **/
    public SecretsTaskLoader(Context context, ArrayList<Secret> secrets) {
        super(context);

        mOriginalSecrets = secrets;
    }

    /** Constructor used when applying (or resetting) a filter to a local set of secrets. **/
    public SecretsTaskLoader(Context context, ArrayList<Secret> secrets, String filter) {
        super(context);

        mFilter = filter;
        mOriginalSecrets = secrets;
    }

    @Override
    public Cursor loadInBackground() {
        // Contains the list of remote secrets.
        ArrayList<Secret> secrets = mOriginalSecrets;

        // When there is a list of secrets, create a MatrixCursor from it and apply a filter if any.
        MatrixCursor cursor = null;
        if (secrets != null) {
            int size = secrets.size();
            if (size > 0) {
                String[] columns = new String[]{BaseColumns._ID, Secret.DOMAIN, Secret.PATH, Secret.USERNAME, Secret.USERNAME_NORMALIZED, Secret.OTP};
                cursor = new MatrixCursor(columns);

                // Keep track of id separately since it may skip one when filtering a secret out.
                int id = 0;
                for (int i = 0; i < size; i++) {
                    Secret secret = secrets.get(i);

                    // Apply filtering.
                    if (mFilter == null || secret.isMatch(mFilter)) {
                        // Add to cursor.
                        MatrixCursor.RowBuilder builder = cursor.newRow();
                        builder.add(BaseColumns._ID, id++);
                        builder.add(Secret.DOMAIN, secret.getDomain());
                        builder.add(Secret.PATH, secret.getPath());
                        builder.add(Secret.USERNAME, secret.getUsername());
                        builder.add(Secret.USERNAME_NORMALIZED, secret.getUsernameNormalized());
                        builder.add(Secret.OTP, secret.getOtp());
                    }
                }

                // Loop back to start of cursor before returning it.
                cursor.moveToFirst();
            }
        }
        return cursor;
    }

    @Override
    public void deliverResult(Cursor cursor) {
        super.deliverResult(cursor);

        if (isReset()) {
            // An async query came in while the loader is stopped.
            if (cursor != null) {
                cursor.close();
            }
            return;
        }
        Cursor oldCursor = mCursor;
        mCursor = cursor;

        if (isStarted()) {
            super.deliverResult(cursor);
        }

        if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    @Override
    protected void onStartLoading() {
        if (mCursor != null) {
            deliverResult(mCursor);
        }
        if (takeContentChanged() || mCursor == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped.
        onStopLoading();

        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        mCursor = null;
    }
}