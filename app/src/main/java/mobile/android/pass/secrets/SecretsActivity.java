package mobile.android.pass.secrets;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import mobile.android.pass.R;
import mobile.android.pass.settings.SettingsActivity;

public class SecretsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private final int LOADER_ID_REFRESH = 0;
    private final int LOADER_ID_FILTER = 1;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SecretsAdapter mSecretsAdapter;
    private ListView mListView;
    private String mCurFilter;
    private ArrayList<Secret> mSecrets = new ArrayList<>();
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secrets);

        // Add back button to action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create the cursor adapter.
        mSecretsAdapter = new SecretsAdapter(this);

        // Attach the adapter to a RecyclerView.
        mListView = (ListView) findViewById(R.id.list_view_secrets);
        mListView.setAdapter(mSecretsAdapter);
        mListView.setFastScrollEnabled(true);

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
                getSupportLoaderManager().initLoader(LOADER_ID_REFRESH, null, SecretsActivity.this);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_secrets, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(item);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setSubmitButtonEnabled(false);

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
        // Drop filter.
        if(mSearchView != null) {
            // Clear filter.
            mSearchView.setQuery("", false);
            // Hide keyboard.
            mSearchView.clearFocus();
            // Switch back to actionbar icon.
            mSearchView.setIconified(true);
        }
        getSupportLoaderManager().restartLoader(LOADER_ID_REFRESH, null, this);
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
        getSupportLoaderManager().restartLoader(LOADER_ID_FILTER, null, this);
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mListView.setVisibility(View.GONE);
        mSwipeRefreshLayout.setRefreshing(true);

        switch (id) {
            case LOADER_ID_REFRESH:
                // Fetch new set of secrets.
                return new ShowSecretsTask(this);
            case LOADER_ID_FILTER:
                // Filter from secrets in memory.
                // FIXME: search and clear filter and the first item is missing ?!
                return new ShowSecretsTask(this, mCurFilter, mSecrets);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d("pass", "onLoadFinished");
        mSwipeRefreshLayout.setRefreshing(false);
        mListView.setVisibility(View.VISIBLE);

        if (loader.getId() == LOADER_ID_REFRESH) {
            // Copy data to array.
            mSecrets.clear();
            data.moveToFirst();
            while(data.moveToNext()) {
                Secret secret = new Secret(data);
                mSecrets.add(secret);
            }
            data.moveToFirst();
        }

        // Swap the new cursor in. (The framework will take care of closing the
        // old cursor once we return.)
        mSecretsAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed. We need to make sure we are no
        // longer using it.
        mSecretsAdapter.swapCursor(null);
    }

    @Override
    public void onClick(View view) {
        int position = (int) view.getTag();
        final Secret secret = new Secret((Cursor) mSecretsAdapter.getItem(position));

        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);

        // Inflating the Popup using xml file.
        popupMenu.getMenuInflater().inflate(R.menu.menu_item_secret, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
             @Override
             public boolean onMenuItemClick(MenuItem item) {
                 switch (item.getItemId()) {
                     case R.id.action_copy_secret_password:
                         Log.i("pass", "Clicked on " + item.getTitle() + ": ********");
                         break;
                     case R.id.action_copy_secret_username:
                         Log.i("pass", "Clicked on " + item.getTitle() + ": " + secret.getUsername());
                         break;
                     case R.id.action_copy_secret_website:
                         Log.i("pass", "Clicked on " + item.getTitle() + ": " + secret.getDomain());
                         break;
                 }
                 return true;
             }
        });

        // Show menu.
        popupMenu.show();

        // Position menu over button.
        ListPopupWindow.ForwardingListener listener = (ListPopupWindow.ForwardingListener) popupMenu.getDragToOpenListener();
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
//        listener.getPopup().setVerticalOffset(- view.getHeight() - lp.topMargin);
        listener.getPopup().setVerticalOffset(- view.getHeight());

        // Redraw on the new position.
        listener.getPopup().show();
    }

    public static class ShowSecretsTask extends AsyncTaskLoader<Cursor> {
        private Cursor mCursor;
        private String mFilter;
        private ArrayList<Secret> mOriginalSecrets;

        public ShowSecretsTask(Context context) {
            super(context);
        }

        public ShowSecretsTask(Context context, String filter, ArrayList<Secret> secrets) {
            this(context);
            mFilter = filter;
            mOriginalSecrets = secrets;
        }

        // Runs on a worker thread .
        @Override
        public Cursor loadInBackground() {
            ArrayList<Secret> secrets = null;

            if(mOriginalSecrets == null) {
                Log.d("pass", "sleeping");
                try {
                    // Simulate network access.
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    return null;
                }

                // Fetching some data, data has now returned
                String json = "[\n";
                char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();
                for (int i = 0; i < alphabet.length; i++) {
                    json += "  {\n" +
                            "    \"domain\": \"" + Character.toString(alphabet[i]) + "\",\n" +
                            "    \"path\": \"gmail.com\",\n" +
                            "    \"username\": \"rcaldwell\",\n" +
                            "    \"username_normalized\": \"rcaldwell\"\n" +
                            "  }";
                    json += ",";
                    json += "  {\n" +
                            "    \"domain\": \"" + Character.toString(alphabet[i]) + "\",\n" +
                            "    \"path\": \"work/bitbucket.org\",\n" +
                            "    \"username\": \"ninapeña\",\n" +
                            "    \"username_normalized\": \"ninapena\"\n" +
                            "  }\n";
                    if (i < (alphabet.length - 1)) {
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
                    secrets = Secret.fromJson(jsonArray);
                }
            } else {
                Log.d("pass", "NOT sleeping");
                secrets = mOriginalSecrets;
            }

            String[] columns = new String[]{BaseColumns._ID, Secret.DOMAIN, Secret.PATH, Secret.USERNAME, Secret.USERNAME_NORMALIZED};
            MatrixCursor cursor = new MatrixCursor(columns);
            int i = 0;
            for(Secret secret : secrets) {
                if(secret.isMatch(mFilter)) {
                    MatrixCursor.RowBuilder builder = cursor.newRow();
                    builder.add(BaseColumns._ID, i++);
                    builder.add(Secret.DOMAIN, secret.getDomain());
                    builder.add(Secret.PATH, secret.getPath());
                    builder.add(Secret.USERNAME, secret.getUsername());
                    builder.add(Secret.USERNAME_NORMALIZED, secret.getUsernameNormalized());
                }
            }

            cursor.moveToFirst();

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
}
