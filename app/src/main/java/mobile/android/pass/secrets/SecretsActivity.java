package mobile.android.pass.secrets;

import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import mobile.android.pass.R;
import mobile.android.pass.settings.SettingsActivity;

public class SecretsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<MatrixCursor> {

    SwipeRefreshLayout mSwipeRefreshLayout;
    SecretsAdapter mSecretsAdapter;
    RecyclerView mRecyclerView;
    String mCurFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secrets);

        // Add back button to action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create the cursor adapter.
        mSecretsAdapter = new SecretsAdapter(this);

        // Attach the adapter to a RecyclerView.
        mRecyclerView = (RecyclerView) findViewById(R.id.recycle_view_secrets);
        mRecyclerView.setAdapter(mSecretsAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Setup the SwipeRefreshLayout.
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_secrets);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.accent);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        // Initial load with animation.
        mSwipeRefreshLayout.post(new Runnable() {
            @Override public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                // Prepare the loader. Either re-connect with an existing one,
                // or start a new one.
                getSupportLoaderManager().initLoader(0, null, SecretsActivity.this);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_secrets, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.open_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRefresh() {
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // Don't care about this.
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Called when the action bar search text has changed.  Update
        // the search filter, and restart the loader to do a new query
        // with this filter.
        String newFilter = !TextUtils.isEmpty(newText) ? newText : null;
        // Don't do anything if the filter hasn't actually changed.
        // Prevents restarting the loader when restoring state.
        if (mCurFilter == null && newFilter == null) {
            return true;
        }
        if (mCurFilter != null && mCurFilter.equals(newFilter)) {
            return true;
        }
        mCurFilter = newFilter;
        getSupportLoaderManager().restartLoader(0, null, this);
        return true;
    }

    @Override
    public Loader<MatrixCursor> onCreateLoader(int id, Bundle args) {
        mSwipeRefreshLayout.setRefreshing(true);
        return new RetrieveSecretsTask(this, mCurFilter);
    }

    @Override
    public void onLoadFinished(Loader<MatrixCursor> loader, MatrixCursor data) {
        mSwipeRefreshLayout.setRefreshing(false);

        // Swap the new cursor in. (The framework will take care of closing the
        // old cursor once we return.)
        mSecretsAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<MatrixCursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed. We need to make sure we are no
        // longer using it.
        mSecretsAdapter.swapCursor(null);
    }

    public static class RetrieveSecretsTask extends AsyncTaskLoader<MatrixCursor> {
        private MatrixCursor mCursor;
        private String mFilter;

        public RetrieveSecretsTask(Context context, String filter) {
            super(context);

            mFilter = filter != null ? filter : "";
        }

        // Runs on a worker thread .
        @Override
        public MatrixCursor loadInBackground() {
            Log.d("pass", "sleeping");
            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return null;
            }

            // Fetching some data, data has now returned
            String secret1 =  "  {\n" +
                    "    \"domain\": \"gmail.com\",\n" +
                    "    \"path\": \"gmail.com\",\n" +
                    "    \"username\": \"rcaldwell\",\n" +
                    "    \"username_normalized\": \"rcaldwell\"\n" +
                    "  }";
            String secret2 =  "  {\n" +
                    "    \"domain\": \"bitbucket.org\",\n" +
                    "    \"path\": \"work/bitbucket.org\",\n" +
                    "    \"username\": \"ninapeña\",\n" +
                    "    \"username_normalized\": \"ninapena\"\n" +
                    "  }\n";
            String json = "[\n";
            int listSize = 4;
            for(int i = 0; i < listSize / 2; i++) {
                json += secret1;
                json += ",";
                json += secret2;
                if (i < (listSize / 2 - 1)) {
                    json += ",";
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
                ArrayList<Secret> secrets = Secret.fromJson(jsonArray);
                String[] columns = new String[] {Secret.DOMAIN, Secret.PATH, Secret.USERNAME, Secret.USERNAME_NORMALIZED};
                MatrixCursor cursor = new MatrixCursor(columns);
                for(Secret secret : secrets) {
                    if (secret.isMatch(mFilter)) {
                        MatrixCursor.RowBuilder builder = cursor.newRow();
                        builder.add(Secret.DOMAIN, secret.getDomain());
                        builder.add(Secret.PATH, secret.getPath());
                        builder.add(Secret.USERNAME, secret.getUsername());
                        builder.add(Secret.USERNAME_NORMALIZED, secret.getUsernameNormalized());
                    }
                }

                return cursor;
            }
            return null;
        }

        @Override
        public void deliverResult(MatrixCursor cursor) {
            super.deliverResult(cursor);

            if (isReset()) {
                // An async query came in while the loader is stopped.
                if (cursor != null) {
                    cursor.close();
                }
                return;
            }
            MatrixCursor oldCursor = mCursor;
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
        public void onCanceled(MatrixCursor cursor) {
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
}
