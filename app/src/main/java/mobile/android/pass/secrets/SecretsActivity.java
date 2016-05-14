package mobile.android.pass.secrets;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
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

public class SecretsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener, LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {
    private static final String TAG = SecretsActivity.class.toString();

    private final int LOADER_ID_REFRESH = 0;
    private final int LOADER_ID_FILTER = 1;
    private final int NO_POPUP = -1;

    // UI references.
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SecretsAdapter mSecretsAdapter;
    private ListView mListView;
    private PopupMenu mPopupMenu;
    private SearchView mSearchView;

    // Save to and restore state from these.
    private Parcelable mListViewState;
    private String mCurFilter;
    private int mPopupMenuViewPosition = NO_POPUP;
    private ArrayList<Secret> mSecrets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_secrets);

        // Add back button to action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Restore from saved state.
        if (savedInstanceState != null) {
            mSecrets = savedInstanceState.getParcelableArrayList("mSecrets");
            mCurFilter = savedInstanceState.getString("mCurFilter");
            mPopupMenuViewPosition = savedInstanceState.getInt("mPopupMenuViewPosition");
        }

        // Create the cursor adapter.
        mSecretsAdapter = new SecretsAdapter(this);

        // Attach the adapter to a ListView.
        mListView = (ListView) findViewById(R.id.list_view_secrets);
        mListView.setAdapter(mSecretsAdapter);
        // TODO: FIXME: Can't fast scroll without triggering swipe-to-refresh.
        mListView.setFastScrollEnabled(true);

        // Setup the SwipeRefreshLayout.
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_secrets);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.accent);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        if (mSecrets == null) {
            // Initial load with animation.
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                    // Prepare the loader. Either re-connect with an existing one,
                    // or start a new one.
                    getSupportLoaderManager().initLoader(LOADER_ID_REFRESH, null, SecretsActivity.this);
                }
            });
        } else {
            // Empty filter will simply show everything.
            // Use restart instead of init to force-create a new loader, which in turn creates a
            // new SecretsTaskLoader with the latest mSecrets as its mOriginalSecrets.
            getSupportLoaderManager().restartLoader(LOADER_ID_FILTER, null, SecretsActivity.this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList("mSecrets", mSecrets);
        outState.putString("mCurFilter", mCurFilter);

        outState.putInt("mPopupMenuViewPosition", mPopupMenuViewPosition);
        if (mPopupMenu != null) {
            mPopupMenu.dismiss();
        }

        mListViewState = mListView.onSaveInstanceState();
        outState.putParcelable("mListViewState", mListViewState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);

        mSecrets = savedInstanceState.getParcelableArrayList("mSecrets");
        mCurFilter = savedInstanceState.getString("mCurFilter");
        mListViewState = savedInstanceState.getParcelable("mListViewState");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_secrets, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(item);
        mSearchView.setOnCloseListener(this);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setSubmitButtonEnabled(false);
//        mSearchView.setIconifiedByDefault(true);

        // Restored from saved state.
        if (mCurFilter != null) {
            mSearchView.setQuery(mCurFilter, false);
            if (!TextUtils.isEmpty(mCurFilter)) {
                mSearchView.setIconified(false);
//            } else {
//                mSearchView.setIconified(true);
            }
        }

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
        Log.d(TAG, "onRefresh");
        // Drop filter.
        if (mSearchView != null) {
            // Clear filter.
            mSearchView.setQuery("", false);
            // Hide keyboard.
            mSearchView.clearFocus();
            // Switch back to actionbar icon.
            mSearchView.setIconified(true);
        }
        getSupportLoaderManager().restartLoader(LOADER_ID_REFRESH, null, this);
    }

    /**
     * This method is executed when pressing the search button on the kayboard and the search button
     * in the text editing mode for landscape orientation.
     * FIXME: dismiss full screen editor.
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.d(TAG, "onQueryTextSubmit");
        // Called when the action bar search text has changed. Update
        // the search filter, and restart the loader to do a new query
        // with this filter.
        String newFilter = !TextUtils.isEmpty(query) ? query : null;
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

    /**
     * This method is executed while typing in the SearchView for portrait orientation.
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        Log.d(TAG, "onQueryTextChange");
        // Called when the action bar search text has changed. Update
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
    public boolean onClose() {
        if (!TextUtils.isEmpty(mSearchView.getQuery())) {
            mSearchView.setQuery(null, true);
        }
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader: " + (id == LOADER_ID_FILTER ? "LOADER_ID_FILTER" : "LOADER_ID_REFRESH"));
        mListView.setVisibility(View.GONE);
        mSwipeRefreshLayout.setRefreshing(true);

        switch (id) {
            case LOADER_ID_REFRESH:
                // Fetch new set of secrets.
                return new SecretsTaskLoader(this);
            case LOADER_ID_FILTER:
                // Filter from secrets in memory.
                // FIXME: the first item is missing after search and clearing filter ?!
                return new SecretsTaskLoader(this, mCurFilter, mSecrets);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished: " + (loader.getId() == LOADER_ID_FILTER ? "LOADER_ID_FILTER" : "LOADER_ID_REFRESH"));
        mSwipeRefreshLayout.setRefreshing(false);
        mListView.setVisibility(View.VISIBLE);

        if (loader.getId() == LOADER_ID_REFRESH) {
            // Copy data to array.
            if (mSecrets == null) {
                mSecrets = new ArrayList<>();
            } else {
                mSecrets.clear();
            }
            data.moveToFirst();
            while (data.moveToNext()) {
                Secret secret = new Secret(data);
                mSecrets.add(secret);
            }
            data.moveToFirst();
        }

        // Swap the new cursor in. (The framework will take care of closing the
        // old cursor once we return.)
        mSecretsAdapter.swapCursor(data);

        // Restore scrolling position in mListView (can be done after swapCursor).
        if (mListViewState != null) {
            mListView.post(new Runnable() {
                @Override
                public void run() {
                    mListView.requestFocus();
                    mListView.onRestoreInstanceState(mListViewState);
                    mListViewState = null;
                }
            });
        }

        // Restore open PopupMenu (can be done after swapCursor).
        if (mPopupMenuViewPosition != NO_POPUP) {
            // Post a Runnable because showPopup anchors to a View that needs to be rendered first.
            mListView.post(new Runnable() {
                @Override
                public void run() {
                    showPopup(mPopupMenuViewPosition);
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset: " + (loader.getId() == LOADER_ID_FILTER ? "LOADER_ID_FILTER" : "LOADER_ID_REFRESH"));
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed. We need to make sure we are no
        // longer using it.
        mSecretsAdapter.swapCursor(null);
    }

    private void showPopup(int popupMenuViewPosition) {
        Log.d(TAG, "showPopup: " + popupMenuViewPosition);

        if (mListView == null) {
            return;
        }

        int firstListItemPosition = mListView.getFirstVisiblePosition();
        int lastListItemPosition = firstListItemPosition + mListView.getChildCount() - 1;

        // Re-use active view or create one using the adapter.
        View view;
        if (popupMenuViewPosition < firstListItemPosition || popupMenuViewPosition > lastListItemPosition) {
            Log.d(TAG, "getView()");
            // FIXME: getView() positions mPopupMenu in the top left corner of the screen when restoring.
            view = mListView.getAdapter().getView(popupMenuViewPosition, null, mListView);
        } else {
            Log.d(TAG, "getChildAt()");
            int childIndex = popupMenuViewPosition - firstListItemPosition;
            view = mListView.getChildAt(childIndex);
        }

        SecretsAdapter.SecretViewHolder holder = (SecretsAdapter.SecretViewHolder) view.getTag();
        View anchorView = holder.getActions();
        Log.d(TAG, "anchorView.width: " + anchorView.getWidth()); // "0" for getView()

        // Inflating the Popup using xml file.
        mPopupMenu = new PopupMenu(anchorView.getContext(), anchorView);
        mPopupMenu.getMenuInflater().inflate(R.menu.menu_item_secret, mPopupMenu.getMenu());

        final Secret secret = new Secret((Cursor) mSecretsAdapter.getItem(popupMenuViewPosition));
        mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
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

        mPopupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                Log.d(TAG, "mPopMenu dismissed");
                mPopupMenu = null;
                mPopupMenuViewPosition = NO_POPUP;
            }
        });

        // Show menu.
        mPopupMenu.show();

        // Re-position immediately so it is positioned on top of anchorView (PopupMenu has no OnShowListener).
        ListPopupWindow.ForwardingListener listener = (ListPopupWindow.ForwardingListener) mPopupMenu.getDragToOpenListener();
        listener.getPopup().setHorizontalOffset(-listener.getPopup().getWidth() + anchorView.getWidth());
        listener.getPopup().setVerticalOffset(-anchorView.getHeight());
        listener.getPopup().show();
    }

    @Override
    public void onClick(View view) {
        mPopupMenuViewPosition = (int) view.getTag();
        showPopup(mPopupMenuViewPosition);
    }
}
