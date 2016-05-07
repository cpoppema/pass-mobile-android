package mobile.android.pass.secrets;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
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
import android.widget.ListView;

import java.util.ArrayList;

import mobile.android.pass.R;
import mobile.android.pass.settings.SettingsActivity;

public class SecretsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private static final String TAG = SecretsActivity.class.toString();
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
                return new SecretsTaskLoader(this);
            case LOADER_ID_FILTER:
                // Filter from secrets in memory.
                // FIXME: search and clear filter and the first item is missing ?!
                return new SecretsTaskLoader(this, mCurFilter, mSecrets);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished");
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
                         Log.i(TAG, "Clicked on " + item.getTitle() + ": ********");
                         break;
                     case R.id.action_copy_secret_username:
                         Log.i(TAG, "Clicked on " + item.getTitle() + ": " + secret.getUsername());
                         break;
                     case R.id.action_copy_secret_website:
                         Log.i(TAG, "Clicked on " + item.getTitle() + ": " + secret.getDomain());
                         break;
                 }
                 return true;
             }
        });

        // Show menu.
        popupMenu.show();

        // Re-position immediately so it is positioned over the view.
        ListPopupWindow.ForwardingListener listener = (ListPopupWindow.ForwardingListener) popupMenu.getDragToOpenListener();
        listener.getPopup().setHorizontalOffset(- listener.getPopup().getWidth() + listener.getPopup().getAnchorView().getWidth());
        listener.getPopup().setVerticalOffset(- view.getHeight());
        listener.getPopup().show();
    }
}
