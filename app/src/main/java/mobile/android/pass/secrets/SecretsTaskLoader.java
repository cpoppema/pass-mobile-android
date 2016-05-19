package mobile.android.pass.secrets;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.BaseColumns;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import mobile.android.pass.api.Api;

public class SecretsTaskLoader extends AsyncTaskLoader<Cursor> {

    private static final String TAG = SecretsActivity.class.toString();
    private Cursor mCursor;
    private String mFilter;
    private ArrayList<Secret> mOriginalSecrets;
    private Api mApi;

    public SecretsTaskLoader(Context context, Api api) {
        super(context);

        mApi = api;
    }

    public SecretsTaskLoader(Context context, String filter, ArrayList<Secret> secrets) {
        super(context);

        mFilter = filter;
        mOriginalSecrets = secrets;
    }

    @Override
    public Cursor loadInBackground() {
        ArrayList<Secret> secrets = null;

        if(mOriginalSecrets == null) {
            Log.d(TAG, "sleeping");
            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return null;
            }

//            if (mApi != null) {
//                secrets = mApi.getSecrets();
//            } else {
                // Fetching some data, data has now returned
                String json = "[\n";
                char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();
                int listSize = 26 * 10;
                for (int i = 0; i < alphabet.length; i++) {
                    for (int j = 0; j < listSize / alphabet.length; j++) {
                        json += "  {\n" +
                                "    \"domain\": \"" + Character.toString(alphabet[i]) + ".com\",\n" +
                                "    \"path\": \"gmail.com\",\n" +
                                "    \"username\": \"rcaldwell\",\n" +
                                "    \"username_normalized\": \"rcaldwell\"\n" +
                                "  }";
                        if ((i * listSize / alphabet.length + j) < listSize - 1) {
                            json += ",";
                        }
                    }
                }
                json += "]";

                JSONArray jsonArray = null;
                try {
                    jsonArray = new JSONArray(json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (jsonArray != null) {
                    secrets = Secret.fromJson(jsonArray);
                }
//            }

        } else {
            Log.d(TAG, "NOT sleeping");
            secrets = mOriginalSecrets;
        }

        MatrixCursor cursor = null;
        if (secrets != null) {
            int id = 0;
            String[] columns = new String[]{BaseColumns._ID, Secret.DOMAIN, Secret.PATH, Secret.USERNAME, Secret.USERNAME_NORMALIZED};
            cursor = new MatrixCursor(columns);
            int size = secrets.size();
            for (int i = 0; i < size; i++) {
                Secret secret = secrets.get(i);
                if (secret.isMatch(mFilter)) {
                    MatrixCursor.RowBuilder builder = cursor.newRow();
                    builder.add(BaseColumns._ID, id++);
                    builder.add(Secret.DOMAIN, secret.getDomain());
                    builder.add(Secret.PATH, secret.getPath());
                    builder.add(Secret.USERNAME, secret.getUsername());
                    builder.add(Secret.USERNAME_NORMALIZED, secret.getUsernameNormalized());
                }
            }

            cursor.moveToFirst();
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

        // Ensure the loader is stopped
        onStopLoading();

        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        mCursor = null;
    }
}