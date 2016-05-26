package mobile.android.pass.secrets;

import org.spongycastle.openpgp.PGPPrivateKey;

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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

import mobile.android.pass.R;
import mobile.android.pass.api.Api;
import mobile.android.pass.settings.SettingsActivity;
import mobile.android.pass.utils.ClipboardHelper;
import mobile.android.pass.utils.PgpHelper;
import mobile.android.pass.utils.StorageHelper;

/** Shows a list of secrets to search in and copy data for (e.g. username, password). **/

public class SecretsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {
    private static final String TAG = SecretsActivity.class.toString();

    // Loader ID for fetching secrets.
    private final int LOADER_ID_REFRESH = 0;
    // Loader ID for filtering last known secrets.
    private final int LOADER_ID_FILTER = 1;
    // Indicates there is no popup visible.
    private final int NO_POPUP = -1;
    // When returning to this activity, it will be closed when this timeout in seconds has expired.
    private final int TIMEOUT_AFTER = 30;

    private Api mApi;
    // Active query text for mSearchView.
    private String mCurFilter;
    // Layout reference.
    private ListView mListView;
    // Restore/save layout state from/to this.
    private Parcelable mListViewState;
    // Passphrase to unlock the key currently in storage.
    private String mPassphrase;
    // View reference.
    private PopupMenu mPopupMenu;
    // Restore/save position from/to this.
    private int mPopupMenuViewPosition = NO_POPUP;
    // Key used to decrypt api responses.
    private PGPPrivateKey mPrivateKey;
    // View reference.
    private SearchView mSearchView;
    // List of most recent secrets retrieved from the api.
    private ArrayList<Secret> mSecrets;
    // ListView adapter reference.
    private SecretsAdapter mSecretsAdapter;
    // Storage reference.
    private StorageHelper mStorageHelper;
    // Layout reference.
    private SwipeRefreshLayout mSwipeRefreshLayout;
    // Time in seconds since the key was unlocked.
    private int mTimeActivated = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        // Load content from XML resource.
        setContentView(R.layout.activity_secrets);

        // Add back button to action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (!TextUtils.isEmpty(getIntent().getStringExtra("mPassphrase"))) {
            // Get passphrase from previous activity.
            mPassphrase = getIntent().getStringExtra("mPassphrase");
            getIntent().removeExtra("mPassphrase");

            // Keep track of time since unlock.
            mTimeActivated = (int) (Calendar.getInstance().getTimeInMillis() / 1000L);
        }
        Log.d(TAG, "mPassphrase: " + mPassphrase);

        // Instantiate custom storage interface.
        mStorageHelper = new StorageHelper(this);

        // Setup the ListView.
        mListView = (ListView) findViewById(R.id.list_view_secrets);
        mSecretsAdapter = new SecretsAdapter(this);
        mListView.setAdapter(mSecretsAdapter);
        mListView.setFastScrollEnabled(true);  // FIXME: Can't fast scroll without triggering swipe-to-refresh.

        // Setup the SwipeRefreshLayout.
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_secrets);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.accent);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        // Load secrets.
        if (mSecrets == null) {
            // Initial load.
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    // Animated busy indicator.
                    mSwipeRefreshLayout.setRefreshing(true);
                    // Prepare the loader.
                    getSupportLoaderManager().initLoader(LOADER_ID_REFRESH, null, SecretsActivity.this);
                }
            });
        } else {
            // Resume with a list of secrets from the bundle. Since mCurFilter is still empty,
            // all secrets will be visible.

            // Use restart instead of init to force-create a new loader, which in turn creates a
            // new SecretsTaskLoader with the latest mSecrets as its mOriginalSecrets.
            getSupportLoaderManager().restartLoader(LOADER_ID_FILTER, null, SecretsActivity.this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.d(TAG, "onSaveInstanceState");

        // Remember last known secrets.
        outState.putParcelableArrayList("mSecrets", mSecrets);

        // Remember current search filter.
        outState.putString("mCurFilter", mCurFilter);

        // Save list state (e.g. scrolling position).
        mListViewState = mListView.onSaveInstanceState();
        outState.putParcelable("mListViewState", mListViewState);

        // Remember if a popup was visible.
        outState.putInt("mPopupMenuViewPosition", mPopupMenuViewPosition);
        if (mPopupMenu != null) {
            // Close it to prevent leaking it.
            mPopupMenu.dismiss();
        }

        // Remember passphrase to be able to unlock the secret key on resume.
        outState.putString("mPassphrase", mPassphrase);

        // Keep track of the original time since unlock.
        outState.putInt("mTimeActivated", mTimeActivated);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState");

        super.onRestoreInstanceState(savedInstanceState);

        // Simply restore everything saved in onSaveInstanceState.
        mSecrets = savedInstanceState.getParcelableArrayList("mSecrets");
        mCurFilter = savedInstanceState.getString("mCurFilter");
        mListViewState = savedInstanceState.getParcelable("mListViewState");
        mPopupMenuViewPosition = savedInstanceState.getInt("mPopupMenuViewPosition");
        mPassphrase = savedInstanceState.getString("mPassphrase");
        mTimeActivated = savedInstanceState.getInt("mTimeActivated");
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.i(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.i(TAG, "onStop");

        // FIXME: Stop ongoing http requests when activity stops.
//        if (mApi != null) {
//            mApi.cancelAll();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "onResume");

        // Check if the key was unlocked more than TIMEOUT_AFTER seconds ago.
        boolean expired = testTimeoutIsExpired();
        if (expired) {
            Log.d(TAG, "Timeout expired, finish activity");

            mPassphrase = null;  // TODO: Find out if this is really necessary.
            // Close activity, returning to the UnlockActivity.
            finish();
        } else {
            Log.d(TAG, "Timeout has not expired yet, keeping activity alive");

            // Get private key from storage.
            mPrivateKey = PgpHelper.extractPrivateKey(mStorageHelper.getPrivateKey(), mPassphrase);
            Log.d(TAG, "Unlocked key: " + Long.toHexString(mPrivateKey.getKeyID()));

//            mApi = new Api(this, mPrivateKey);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");

        // Inflate from XML resource.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_secrets, menu);

        // Setup the SearchView.
        MenuItem item = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(item);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setSubmitButtonEnabled(false);
//        mSearchView.setIconifiedByDefault(true);

        // Restore search filter.
        if (mCurFilter != null) {
            mSearchView.setQuery(mCurFilter, false);
            if (TextUtils.isEmpty(mCurFilter)) {
                mSearchView.setIconified(true);
            } else {
//                mSearchView.setIconified(false);
            }
        } else {
            mSearchView.setIconified(true);
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
                Log.i(TAG, "Start SettingsActivity");
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
     * Filters the list of secrets when pressing the search button on the keyboard and the search
     * button while in the text editing mode when landscape orientation is active.
     * FIXME: Dismiss full screen editor.
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
     * Filters the list of secrets while typing in the SearchView when portrait orientation is
     * active.
     * FIXME: Contains the same code as onQueryTextSubmit.
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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader: " + (id == LOADER_ID_FILTER ? "LOADER_ID_FILTER" : "LOADER_ID_REFRESH"));

        // Hide the ListView while retrieving secrets to prevent interaction with it while the
        // loading animation is showing.
        mListView.setVisibility(View.GONE);
        mSwipeRefreshLayout.setRefreshing(true);

        switch (id) {
            case LOADER_ID_REFRESH:
                // Fetch new set of secrets.
                return new SecretsTaskLoader(this, mApi);
            case LOADER_ID_FILTER:
                // Filter from secrets in memory.
                // FIXME: Looks like the first item is missing after search and clearing filter.
                return new SecretsTaskLoader(this, mCurFilter, mSecrets);
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished: " + (loader.getId() == LOADER_ID_FILTER ? "LOADER_ID_FILTER" : "LOADER_ID_REFRESH"));

        // Hide busy indicator and make the ListView visible again.
        mSwipeRefreshLayout.setRefreshing(false);
        mListView.setVisibility(View.VISIBLE);

        // Overwrite last known results (if any).
        if (loader.getId() == LOADER_ID_REFRESH) {
            // Copy data to array.
            if (mSecrets == null) {
                mSecrets = new ArrayList<>();
            } else {
                mSecrets.clear();
            }

            // Keep track of all secrets to be able to filter them without having to retrieve them
            // again later.
            if (data != null) {
                data.moveToFirst();
                while (data.moveToNext()) {
                    Secret secret = new Secret(data);
                    mSecrets.add(secret);
                }
                data.moveToFirst();
            }
        }

        // Swap the new cursor in.
        mSecretsAdapter.swapCursor(data);

        // Restore scrolling position in the ListView.
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

        // Restore open PopupMenu.
        if (mPopupMenuViewPosition != NO_POPUP) {
            showPopup(mPopupMenuViewPosition);
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

    /** Tests if it has been TIMEOUT_AFTER seconds since the private key was unlocked. **/
    private boolean testTimeoutIsExpired() {
        int timeActive = ((int) (Calendar.getInstance().getTimeInMillis() / 1000L)) - mTimeActivated;
        Log.d(TAG, "Active for: " + Integer.toString(timeActive) + " seconds");

        return (mPassphrase == null || timeActive > TIMEOUT_AFTER);
    }

    /** Shows the PopupMenu for the view at position @popupMenuViewPosition in the ListView. **/
    private void showPopup(final int popupMenuViewPosition) {
        Log.d(TAG, "showPopup: " + popupMenuViewPosition);

        if (mListView == null) {
            return;
        }

        // Re-use active view or create one using the adapter.
        View view;
        int firstListItemPosition = mListView.getFirstVisiblePosition();
        int lastListItemPosition = firstListItemPosition + mListView.getChildCount() - 1;

        if (popupMenuViewPosition < firstListItemPosition || popupMenuViewPosition > lastListItemPosition) {
            // Get the View for the not yet rendered position @popupMenuViewPosition.
            Log.d(TAG, "getView()");

            // FIXME: getView() positions mPopupMenu in the top left corner of the screen when restoring.
            view = mListView.getAdapter().getView(popupMenuViewPosition, null, mListView);
        } else {
            // Get the already available View for position @popupMenuViewPosition.
            Log.d(TAG, "getChildAt()");

            int childIndex = popupMenuViewPosition - firstListItemPosition;
            view = mListView.getChildAt(childIndex);
        }

        // Get the View to anchor the PopupMenu to from the ViewHolder.
        SecretsAdapter.SecretViewHolder holder = (SecretsAdapter.SecretViewHolder) view.getTag();
        final View anchorView = holder.getActions();

        // Inflate from XML resource.
        mPopupMenu = new PopupMenu(anchorView.getContext(), anchorView);
        mPopupMenu.getMenuInflater().inflate(R.menu.menu_item_secret, mPopupMenu.getMenu());

        mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Secret secret = new Secret((Cursor) mSecretsAdapter.getItem(popupMenuViewPosition));

                switch (item.getItemId()) {
                    case R.id.action_copy_secret_password:
                        Log.i(TAG, "Clicked on " + item.getTitle() + "");

                        // TODO: Retrieve password from api.
                        String password = null;
//                        String password = mApi.getSecret(secret);
                        if (password != null) {
                            ClipboardHelper.copy(getApplicationContext(), password);
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.toast_copy_secret_password), Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.toast_copy_secret_password_error), Toast.LENGTH_SHORT)
                                    .show();
                        }
                        break;
//                    case R.id.action_show_secret_password:
//                        Log.i(TAG, "Clicked on " + item.getTitle() + "");
//
//                        // TODO: Retrieve password from api.
//                        String password = null;
////                        String password = mApi.getSecret(secret);
//                        if (password != null) {
//                            // TODO: Show password.
//
//                            Toast.makeText(getApplicationContext(),
//                                    getString(R.string.toast_show_secret_password), Toast.LENGTH_SHORT)
//                                    .show();
//                        } else {
//                            Toast.makeText(getApplicationContext(),
//                                    getString(R.string.toast_show_secret_password_error), Toast.LENGTH_SHORT)
//                                    .show();
//                        }
//                        break;
                    case R.id.action_copy_secret_username:
                        Log.i(TAG, "Clicked on " + item.getTitle() + ": " + secret.getUsername());

                        ClipboardHelper.copy(getApplicationContext(), secret.getUsername());
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.toast_copy_secret_username), Toast.LENGTH_SHORT)
                                .show();
                        break;
                    case R.id.action_copy_secret_website:
                        Log.i(TAG, "Clicked on " + item.getTitle() + ": " + secret.getDomain());

                        ClipboardHelper.copy(getApplicationContext(), secret.getDomain());
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.toast_copy_secret_website), Toast.LENGTH_SHORT)
                                .show();
                        break;
                }
                return true;
            }
        });

        // Remember when this popup is closed.
        mPopupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                Log.d(TAG, "mPopMenu dismissed");

                mPopupMenu = null;
                mPopupMenuViewPosition = NO_POPUP;
            }
        });

        // Show the PopupMenu inside a Runnable because it is anchored to a View that might not be
        // rendered yet.
        anchorView.post(new Runnable() {
            @Override
            public void run() {
                // Check for finishing state in case the timeout expired.
                if (!isFinishing()) {
                    // Show menu.
                    mPopupMenu.show();

                    // Re-position immediately so it is positioned on top of anchorView (PopupMenu has no
                    // OnShowListener).
                    ListPopupWindow.ForwardingListener listener = (ListPopupWindow.ForwardingListener) mPopupMenu.getDragToOpenListener();
                    Log.d(TAG, "Popup horizontal offset: -" + listener.getPopup().getWidth() + " + " + anchorView.getWidth());
                    listener.getPopup().setHorizontalOffset(-listener.getPopup().getWidth() + anchorView.getWidth());
                    Log.d(TAG, "Popup vertical offset: -" + anchorView.getHeight());
                    listener.getPopup().setVerticalOffset(-anchorView.getHeight());
                    listener.getPopup().show();
                }
            }
        });
    }

    /** Shows a PopupMenu with possible actions for the secret clicked on. **/
    @Override
    public void onClick(View view) {
        mPopupMenuViewPosition = (int) view.getTag();
        showPopup(mPopupMenuViewPosition);
    }
}
