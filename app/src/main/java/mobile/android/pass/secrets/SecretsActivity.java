package mobile.android.pass.secrets;

import org.json.JSONArray;
import org.json.JSONException;
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
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

import mobile.android.pass.R;
import mobile.android.pass.api.SecretApi;
import mobile.android.pass.api.SecretCallback;
import mobile.android.pass.api.SecretsApi;
import mobile.android.pass.api.SecretsCallback;
import mobile.android.pass.settings.SettingsActivity;
import mobile.android.pass.unlock.UnlockActivity;
import mobile.android.pass.utils.ClipboardHelper;
import mobile.android.pass.utils.PgpHelper;
import mobile.android.pass.utils.StorageHelper;

/** Shows a list of secrets to search in and copy data for (e.g. username, password). **/

public class SecretsActivity extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener,
        LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener, SecretsCallback,
        SecretCallback,  PopupMenu.OnMenuItemClickListener {
    private static final String TAG = SecretsActivity.class.toString();
    // Indicates the dialog displaying a secret.
    private static int SECRET_DIALOG_TAG = 0;

    // Loader ID for fetching secrets.
    private final int LOADER_ID_REFRESH = 0;
    // Loader ID for filtering last known secrets.
    private final int LOADER_ID_FILTER = 1;
    // Indicates there is no secret used for a popup or dialog.
    private final int NO_ACTIVE_SECRET = -1;
    // When returning to this activity, it will be closed when this timeout in seconds has expired.
    private final int TIMEOUT_AFTER = 30;

    // Restore/save position from/to this.
    private int mCurrentSecretPosition = NO_ACTIVE_SECRET;
    // Indicates what to do when the API gets a response.
    private int mCurrentSecretAction = -1;
    // Layout reference.
    private ListView mListView;
    // Restore/save layout state from/to this.
    private Parcelable mListViewState;
    // Passphrase to unlock the key currently in storage.
    private String mPassphrase;
    // View reference.
    private PopupMenu mPopupMenu;
    // Key used to decrypt API responses.
    private PGPPrivateKey mPrivateKey;
    // Active query text for mSearchView.
    private String mSearchFilter;
    // View reference.
    private SearchView mSearchView;
    // List of most recent secrets retrieved from the api.
    private ArrayList<Secret> mSecrets;
    // API for getting a secret.
    private SecretApi mSecretApi;
    // ListView adapter reference.
    private SecretsAdapter mSecretsAdapter;
    // API for getting secrets index.
    private SecretsApi mSecretsApi;
    // Indicates if a popup for a secret is visible.
    private boolean mShowingPopupMenu = false;
    // Storage reference.
    private StorageHelper mStorageHelper;
    // Layout reference.
    private SwipeRefreshLayout mSwipeRefreshLayout;
    // Time in seconds since the key was unlocked.
    private int mTimeActivated = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        // Load content from XML resource.
        setContentView(R.layout.activity_secrets);

        // Add back button to action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Instantiate custom storage interface.
        mStorageHelper = new StorageHelper(this);

        if (!TextUtils.isEmpty(getIntent().getStringExtra("mPassphrase"))) {
            // Get passphrase from previous activity.
            mPassphrase = getIntent().getStringExtra("mPassphrase");
            getIntent().removeExtra("mPassphrase");

            mPrivateKey = PgpHelper.extractPrivateKey(mStorageHelper.getPrivateKey(), mPassphrase);

            // Keep track of time since unlock.
            mTimeActivated = (int) (Calendar.getInstance().getTimeInMillis() / 1000L);
        }

        // API references, set this activity as response callback.
        mSecretApi = new SecretApi(this, this);
        mSecretsApi = new SecretsApi(this, this);

        // Set up list view ..
        mListView = (ListView) findViewById(R.id.list_view_secrets);
        mSecretsAdapter = new SecretsAdapter(this);
        mListView.setAdapter(mSecretsAdapter);
        mListView.setFastScrollEnabled(true);  // FIXME: Can't fast scroll without triggering swipe-to-refresh.

        // .. and the wrapping swipe layout.
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_secrets);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.accent);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        if (savedInstanceState == null) {
            // Only do the initial network request if it is certain there is not saved state.
            showOrFetchSecrets();
        }
    }

    private void showOrFetchSecrets() {
        Log.d(TAG, "showOrFetchSecrets");
        if (mSecrets == null) {
            // Initial load.
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    // Get list of secrets from server.
                    if (!mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(true);
                    }
                    mSecretsApi.getSecrets();
                }
            });
        } else {
            // Resume with a list of secrets from the bundle. Since mSearchFilter is still empty,
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
        outState.putString("mSearchFilter", mSearchFilter);

        // Save list state (e.g. scrolling position).
        mListViewState = mListView.onSaveInstanceState();
        outState.putParcelable("mListViewState", mListViewState);

        // Remember if a popup was visible.
        outState.putInt("mCurrentSecretPosition", mCurrentSecretPosition);
        outState.putBoolean("mShowingPopupMenu", mShowingPopupMenu);
        if (mPopupMenu != null) {
            // Close it to prevent leaking it.
            mPopupMenu.dismiss();
        }

        // Remember passphrase to be able to unlock the secret key on resume.
        outState.putString("mPassphrase", mPassphrase);

        // Reset timeout to avoid being locked out after a screen rotate.
        mTimeActivated = (int) (Calendar.getInstance().getTimeInMillis() / 1000L);
        // Keep track of the original time since unlock.
        outState.putInt("mTimeActivated", mTimeActivated);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Log.d(TAG, "onRestoreInstanceState");

        // Simply restore everything saved in onSaveInstanceState.
        mSecrets = savedInstanceState.getParcelableArrayList("mSecrets");
        mSearchFilter = savedInstanceState.getString("mSearchFilter");
        mListViewState = savedInstanceState.getParcelable("mListViewState");
        mCurrentSecretPosition = savedInstanceState.getInt("mCurrentSecretPosition");
        mShowingPopupMenu = savedInstanceState.getBoolean("mShowingPopupMenu");
        mPassphrase = savedInstanceState.getString("mPassphrase");
        mTimeActivated = savedInstanceState.getInt("mTimeActivated");

        // Do this in onRestoreInstanceState most of the time to prevent network requests.
        showOrFetchSecrets();
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

        // Stop any queued http requests/make sure response listeners are not called for ongoing
        // requests.
        if (mSecretApi != null) {
            mSecretApi.cancelAll();
        }
        if (mSecretsApi != null) {
            mSecretsApi.cancelAll();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "onResume");

        // Check if the key was unlocked more than TIMEOUT_AFTER seconds ago.
        boolean expired = testTimeoutIsExpired();
        if (expired) {
            Log.d(TAG, "Timeout expired, finish activity");

            // Close activity, returning to the UnlockActivity.
            startActivity(new Intent(this, UnlockActivity.class));
            finish();
        } else {
            Log.d(TAG, "Timeout has not expired yet, keeping activity alive");

            // Get private key from storage.
            mPrivateKey = PgpHelper.extractPrivateKey(mStorageHelper.getPrivateKey(), mPassphrase);
            Log.d(TAG, "Unlocked key ID: " + Long.toHexString(mPrivateKey.getKeyID()).toUpperCase());

            // Requests were canceled, but we never got anything yet.
            if (mSwipeRefreshLayout.isRefreshing()) {
                showOrFetchSecrets();
            }
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
        if (mSearchFilter != null) {
            mSearchView.setQuery(mSearchFilter, false);
            if (TextUtils.isEmpty(mSearchFilter)) {
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

        mSwipeRefreshLayout.setRefreshing(true);
        mSecretsApi.getSecrets();
    }

    /**
     * Filters the list of secrets when pressing the search button on the keyboard and the search
     * button while in the text editing mode when landscape orientation is active.
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.d(TAG, "onQueryTextSubmit");

        // Dismiss keyboard.
        mSearchView.clearFocus();

        return updateFilterAndRestartLoader(query);
    }

    /**
     * Filters the list of secrets while typing in the SearchView when portrait orientation is
     * active.
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        Log.d(TAG, "onQueryTextChange");

        return updateFilterAndRestartLoader(newText);
    }

    /**
     * Filter the list of secret when the current search filter has changed.
     */
    private boolean updateFilterAndRestartLoader(String filterText) {
        // Called when the action bar search text has changed. Update
        // the search filter, and restart the loader to do a new query
        // with this filter.
        String newFilter = !TextUtils.isEmpty(filterText) ? filterText : null;

        // Prevents restarting the loader when restoring state.
        if (mSearchFilter == null && newFilter == null) {
            return true;
        }
        // Don't do anything if the filter hasn't actually changed.
        if (mSearchFilter != null && mSearchFilter.equals(newFilter)) {
            return true;
        }

        mSearchFilter = newFilter;
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
                return new SecretsTaskLoader(this);
            case LOADER_ID_FILTER:
                // Filter from secrets in memory.
                return new SecretsTaskLoader(this, mSearchFilter, mSecrets);
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
                    // Restore the ListView to a position any dialog or popup was visible for.
                    if (mShowingPopupMenu) {
                        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                            private int timesScrolled = 0;

                            @Override
                            public void onScrollStateChanged(AbsListView view, int scrollState) {

                            }

                            @Override
                            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                                // Calling onRestoreInstanceState will trigger onScroll twice:
                                // once when the items are loaded and again after the position is
                                // restored. Show the popup after the second action happens and make
                                // sure the position is actually on screen (it might not show with
                                // a lower visibleItemCount).
                                timesScrolled++;
                                if (timesScrolled > 1) {
                                    if (mCurrentSecretPosition >= firstVisibleItem && mCurrentSecretPosition < firstVisibleItem + visibleItemCount) {
                                        showPopup();
                                        // Don't do this again! At least until the next config change.
                                        mListView.setOnScrollListener(null);
                                    }
                                }
                            }
                        });
                    }

                    mListView.requestFocus();
                    mListView.onRestoreInstanceState(mListViewState);
                    mListViewState = null;

                    // Make sure mPopupMenuPosition is actually on screen and not rendered
                    // just below the current viewport.
                    if (mCurrentSecretPosition > mListView.getLastVisiblePosition()) {
                        mListView.setSelection(mCurrentSecretPosition);
                    }
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

    /**
     * Tests if it has been TIMEOUT_AFTER seconds since the private key was unlocked.
     */
    private boolean testTimeoutIsExpired() {
        int timeActive = ((int) (Calendar.getInstance().getTimeInMillis() / 1000L)) - mTimeActivated;
        Log.d(TAG, "Active for: " + Integer.toString(timeActive) + " seconds");

        return (mPassphrase == null || timeActive > TIMEOUT_AFTER);
    }

    /**
     * Shows the PopupMenu for the view at position @mCurrentSecretPosition in the ListView.
     */
    private void showPopup() {
        if (mListView == null) {
            return;
        }

        // Get the already available View for position @popupMenuViewPosition.
        int childIndex = mCurrentSecretPosition - mListView.getFirstVisiblePosition();
        View view = mListView.getChildAt(childIndex);

        // Get the View to anchor the PopupMenu to from the ViewHolder.
        SecretsAdapter.SecretViewHolder holder = (SecretsAdapter.SecretViewHolder) view.getTag();
        final View anchorView = holder.getActions();

        // Inflate from XML resource.
        mPopupMenu = new PopupMenu(anchorView.getContext(), anchorView);
        mPopupMenu.getMenuInflater().inflate(R.menu.menu_item_secret, mPopupMenu.getMenu());
        mPopupMenu.setOnMenuItemClickListener(this);

        // Remember when this popup is closed.
        mPopupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                Log.d(TAG, "mPopMenu dismissed");

                mPopupMenu = null;
                mShowingPopupMenu = false;
                // Cannot reset @mCurrentSecretPosition because clicking an item or clicking
                // outside the popup to close it will both touch this listener.
            }
        });

        // Check for finishing state in case the timeout expired.
        if (!isFinishing()) {
            // Show menu.
            mShowingPopupMenu = true;
            mPopupMenu.show();

            // Re-position immediately so it is positioned on top of anchorView (PopupMenu has no
            // OnShowListener).
            ListPopupWindow.ForwardingListener listener = (ListPopupWindow.ForwardingListener) mPopupMenu.getDragToOpenListener();
            listener.getPopup().setHorizontalOffset(-listener.getPopup().getWidth() + anchorView.getWidth());
            listener.getPopup().setVerticalOffset(-anchorView.getHeight());
            listener.getPopup().show();
        }
    }

    /**
     * Do something when clicking on different parts of a single list item.
     */
    @Override
    public void onClick(View view) {
        // Remember what secret was clicked on.
        mCurrentSecretPosition = (int) view.getTag();

        switch(view.getId()) {
            case R.id.item_secret:
                // Show a dialog with credentials when clicking on the item itself.
                mCurrentSecretAction = R.id.action_show_secret;
                Secret secret = new Secret((Cursor) mSecretsAdapter.getItem(mCurrentSecretPosition));
                mSecretApi.getSecret(secret.getPath(), secret.getUsername());
                break;
            case R.id.item_secret_actions:
                // Show a popup with next actions.
                showPopup();
                break;
        }
    }

    /**
     * Process a successful response for a single secret.
     */
    @Override
    public void onSecretApiSuccess(String pgpResponse) {
        Log.d(TAG, "onSecretApiSuccess for " + mCurrentSecretPosition);
        Secret secret = new Secret((Cursor) mSecretsAdapter.getItem(mCurrentSecretPosition));

        String secretText = PgpHelper.decrypt(mPrivateKey, pgpResponse);
        if (secretText == null) {
            String errorMessage = getString(R.string.volley_decrypt_response_error_message);
            onSecretApiFailure(errorMessage);
        } else {
            handleSecretActions(secret, secretText);
        }

        // Reset temporary variables.
        mCurrentSecretAction = -1;
        mCurrentSecretPosition = NO_ACTIVE_SECRET;
    }

    @Override
    public void onSecretApiFailure(String errorMessage) {
        Toast.makeText(getApplicationContext(),
                errorMessage, Toast.LENGTH_LONG)
                .show();

        // Reset temporary variables.
        mCurrentSecretAction = -1;
        mCurrentSecretPosition = NO_ACTIVE_SECRET;
    }

    /**
     * Process a successful response for the full secrets list.
     */
    @Override
    public void onSecretsApiSuccess(String pgpResponse) {
        Log.d(TAG, "onSecretsApiSuccess");

        String secretText = PgpHelper.decrypt(mPrivateKey, pgpResponse);
        if (secretText == null) {
            String errorMessage = "Failed to decrypt response";
            onSecretsApiFailure(errorMessage);
        } else {
            try {
                JSONArray secretsJsonArray = new JSONArray(secretText);
                mSecrets = Secret.fromJson(secretsJsonArray);
                getSupportLoaderManager().restartLoader(LOADER_ID_FILTER, null, this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSecretsApiFailure(String errorMessage) {
        mSwipeRefreshLayout.setRefreshing(false);

        Toast.makeText(getApplicationContext(),
                errorMessage, Toast.LENGTH_LONG)
                .show();
    }

    /**
     * Show a popup or dialog depending on what is clicked on in the list.
     */
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        mCurrentSecretAction = item.getItemId();
        Secret secret = new Secret((Cursor) mSecretsAdapter.getItem(mCurrentSecretPosition));
        switch (mCurrentSecretAction) {
            case R.id.action_copy_secret_password:
                mSecretApi.getSecret(secret.getPath(), secret.getUsername());
                break;
            case R.id.action_show_secret:
                mSecretApi.getSecret(secret.getPath(), secret.getUsername());
                break;
            default:
                handleNonSecretActions(secret);

                // Reset temporary variables.
                mCurrentSecretAction = -1;
                mCurrentSecretPosition = NO_ACTIVE_SECRET;
        }

        return true;
    }

    /**
     * Process popup actions that involve fetching the secret first.
     */
    private void handleSecretActions(Secret secret, String secretText) {
        switch (mCurrentSecretAction) {
            case R.id.action_copy_secret_password:
                if (secretText != null) {
                    secret.setSecretText(secretText);
                    ClipboardHelper.copy(getApplicationContext(), secret.getPassphrase());
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.toast_copy_secret_password), Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.toast_copy_secret_password_error), Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case R.id.action_show_secret:
                if (secretText != null) {
                    // Show a dialog.
                    SecretFragment fragment = SecretFragment.newInstance(secret, secretText);
                    fragment.show(getSupportFragmentManager(), "" + SECRET_DIALOG_TAG);
                } else {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.toast_show_secret_dialog_error), Toast.LENGTH_SHORT)
                            .show();
                }
                break;
        }
    }

    /**
     * Process popup actions that don't require fetching the secret at all.
     */
    private void handleNonSecretActions(Secret secret) {
        switch (mCurrentSecretAction) {
            case R.id.action_copy_secret_username:
                ClipboardHelper.copy(getApplicationContext(), secret.getUsername());
                Toast.makeText(getApplicationContext(),
                        getString(R.string.toast_copy_secret_username), Toast.LENGTH_SHORT)
                        .show();
                break;
            case R.id.action_copy_secret_website:
                ClipboardHelper.copy(getApplicationContext(), secret.getDomain());
                Toast.makeText(getApplicationContext(),
                        getString(R.string.toast_copy_secret_website), Toast.LENGTH_SHORT)
                        .show();
                break;
        }

    }
}
