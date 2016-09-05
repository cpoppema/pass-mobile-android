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
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import mobile.android.pass.R;
import mobile.android.pass.api.SecretApi;
import mobile.android.pass.api.SecretCallback;
import mobile.android.pass.api.SecretsApi;
import mobile.android.pass.api.SecretsCallback;
import mobile.android.pass.settings.SettingsActivity;
import mobile.android.pass.unlock.UnlockActivity;
import mobile.android.pass.utils.ClipboardHelper;
import mobile.android.pass.utils.MeasurementHelper;
import mobile.android.pass.utils.PgpHelper;
import mobile.android.pass.utils.StorageHelper;

/** Shows a list of secrets to search in and copy data for (e.g. username, password). **/

public class SecretsActivity extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener,
        LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener, SecretsCallback,
        SecretCallback, AdapterView.OnItemClickListener {
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
    private String[] mSecretActions;
    // Layout reference.
    private ListView mListView;
    // Restore/save layout state from/to this.
    private Parcelable mListViewState;
    // Passphrase to unlock the key currently in storage.
    private String mPassphrase;
    // View reference.
    private ListPopupWindow mPopupWindow;
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

        mSecretActions = getResources().getStringArray(R.array.menu_item_secret_actions);

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

        // .. and the wrapping swipe layout.
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_secrets);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.accent);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        // Only do the initial network request if it is certain there is not a saved state.
        if (savedInstanceState == null) {
            // Start loading when the UI is ready to display a busy indicator.
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    getSupportLoaderManager().initLoader(LOADER_ID_REFRESH, null, SecretsActivity.this);
                }
            });
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
        if (mPopupWindow != null) {
            // Close it to prevent leaking it.
            mPopupWindow.dismiss();
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
        // Start loading when the UI is ready to display a busy indicator.
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                getSupportLoaderManager().restartLoader(LOADER_ID_FILTER, null, SecretsActivity.this);
            }
        });
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
            if (mSwipeRefreshLayout.isRefreshing() && mSecrets == null) {
                mSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Resuming, but layout was still refreshing, issue new refresh task loader");
                        getSupportLoaderManager().restartLoader(LOADER_ID_REFRESH, null, SecretsActivity.this);
                    }
                });
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
        mSearchView.setIconifiedByDefault(true);

        // Restore search filter.
        if (mSearchFilter != null) {
            mSearchView.setQuery(mSearchFilter, false);
            if (!TextUtils.isEmpty(mSearchFilter)) {
                mSearchView.setIconified(false);
                mSearchView.clearFocus();
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

        // Reset mSecrets so that the loader will perform a network request, but a reset of
        // mSearchFilter won't finish up for LOADER_ID_FILTER in onLoadFinished.
        mSecrets = null;

        // Drop filter.
        if (mSearchView != null) {
            if (!TextUtils.isEmpty(mSearchFilter)) {
                // Clear filter.
                mSearchView.setQuery("", false);
            }
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
                // Show unfiltered list of existing secrets or fetch a new set of secrets.
                // Get list of secrets from server.
                if (mSecrets == null) {
                    // Disable search while fetching.
                    if (mSearchView != null) {
                        mSearchView.setEnabled(false);
                    }
                    // Start fetching.
                    mSecretsApi.getSecrets();
                }

                return new SecretsTaskLoader(this, mSecrets);
            case LOADER_ID_FILTER:
                // Filter from secrets in memory.
                return new SecretsTaskLoader(this, mSecrets, mSearchFilter);
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished: " + (loader.getId() == LOADER_ID_FILTER ? "LOADER_ID_FILTER" : "LOADER_ID_REFRESH"));

        if (data == null) {
            // Task was started, but never finished with a result set, most likely because an api
            // call was started, so wait with doing anything until there is data.
            Log.d(TAG, "onLoadFinished without data, return early");
            return;
        } else {
            Log.d(TAG, "onLoadFinished with data");
        }

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
            data.moveToFirst();
            while (!data.isAfterLast()) {
                Secret secret = new Secret(data);
                mSecrets.add(secret);
                data.moveToNext();
            }
            data.moveToFirst();
        } else if (loader.getId() == LOADER_ID_FILTER) {
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

        // Swap the new cursor in.
        mSecretsAdapter.swapCursor(data);

        // Enable search after fetching/restoring.
        if (mSearchView != null) {
            mSearchView.setEnabled(true);
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
        View itemView = mListView.getChildAt(childIndex);

        // Get the View to anchor the PopupMenu to from the ViewHolder.
        SecretsAdapter.SecretViewHolder holder = (SecretsAdapter.SecretViewHolder) itemView.getTag();
        final View anchorView = holder.getActions();

        // Inflate.
        mPopupWindow = new ListPopupWindow(anchorView.getContext());
        mPopupWindow.setAnchorView(anchorView);
        mPopupWindow.setOnItemClickListener(this);

        ListAdapter adapter = new ArrayAdapter<>(anchorView.getContext(), R.layout.item_actions, R.id.title, mSecretActions);
        mPopupWindow.setAdapter(adapter);

        // Remember when this popup is closed.
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                Log.d(TAG, "mPopMenu dismissed");

                mPopupWindow = null;
                mShowingPopupMenu = false;
                // Cannot reset @mCurrentSecretPosition because clicking an item or clicking
                // outside the popup to close it will both touch this listener.
            }
        });

        // WRAP_CONTENT will set the width to the same width of the anchored view, set it manually.
        int width = MeasurementHelper.measureArrayContentWidth(adapter, anchorView.getContext());
        mPopupWindow.setWidth(width);

        // Check for finishing state in case the timeout expired.
        if (!isFinishing()) {
            // Show menu.
            mShowingPopupMenu = true;
            mPopupWindow.show();

            // Re-position immediately so it is positioned on top of anchorView (PopupMenu has no
            // OnShowListener).
            mPopupWindow.setHorizontalOffset(-mPopupWindow.getWidth() + anchorView.getWidth());
            mPopupWindow.setVerticalOffset(-anchorView.getHeight());
            mPopupWindow.show();
        }
    }

    /**
     * Do something when clicking on different parts of a single list item.
     */
    @Override
    public void onClick(View view) {
        // Remember what secret was clicked on.
        mCurrentSecretPosition = (int) view.getTag();

        switch (view.getId()) {
            case R.id.item_secret:
                // Show a dialog with credentials when clicking on the item itself.
                mCurrentSecretAction = 3;  // show password
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

        // Prevent overlap when there is a noticeable delay, e.g. when this callback is
        // called a second time, mCurrentSecretAction would be -1.
        mSecretApi.cancelAll();

        // Reset temporary variables.
        mCurrentSecretAction = -1;
        mCurrentSecretPosition = NO_ACTIVE_SECRET;
    }

    @Override
    public void onSecretApiFailure(String errorMessage) {
        Log.d(TAG, "onSecretApiFailure");

        Toast.makeText(getApplicationContext(),
                errorMessage, Toast.LENGTH_LONG)
                .show();

        // Prevent overlap when there is a noticeable delay, e.g. when this callback is
        // called a second time, mCurrentSecretAction would be -1.
        mSecretApi.cancelAll();

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
            String errorMessage = getString(R.string.volley_decrypt_response_error_message);
            onSecretsApiFailure(errorMessage);
        } else {
            try {
                JSONArray secretsJsonArray = new JSONArray(secretText);
                mSecrets = Secret.fromJson(secretsJsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (mSecrets != null) {
                getSupportLoaderManager().restartLoader(LOADER_ID_REFRESH, null, this);
            }
        }
    }

    @Override
    public void onSecretsApiFailure(String errorMessage) {
        Log.d(TAG, "onSecretsApiFailure");

        mSwipeRefreshLayout.setRefreshing(false);

        Toast.makeText(getApplicationContext(),
                errorMessage, Toast.LENGTH_LONG)
                .show();
    }

    /**
     * Show a popup or dialog depending on what is clicked on in the list.
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        // Immediately dismiss popup.
        mPopupWindow.dismiss();

        // Get secrets for list item.
        Secret secret = new Secret((Cursor) mSecretsAdapter.getItem(mCurrentSecretPosition));

        // Do action.
        mCurrentSecretAction = Arrays.asList(mSecretActions).indexOf(((TextView) view.findViewById(R.id.title)).getText());
        switch (mCurrentSecretAction) {
            case 0:  // copy password
                mSecretApi.getSecret(secret.getPath(), secret.getUsername());
                break;
            case 3:  // show password
                mSecretApi.getSecret(secret.getPath(), secret.getUsername());
                break;
            default:
                handleNonSecretActions(secret);

                // Reset temporary variables.
                mCurrentSecretAction = -1;
                mCurrentSecretPosition = NO_ACTIVE_SECRET;
        }
    }

    /**
     * Process popup actions that involve fetching the secret first.
     */
    private void handleSecretActions(Secret secret, String secretText) {
        switch (mCurrentSecretAction) {
            case 0:  // copy password
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
            case 3:  // show password
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
            case 1:  // copy username
                ClipboardHelper.copy(getApplicationContext(), secret.getUsername());
                Toast.makeText(getApplicationContext(),
                        getString(R.string.toast_copy_secret_username), Toast.LENGTH_SHORT)
                        .show();
                break;
            case 2:  // copy website
                ClipboardHelper.copy(getApplicationContext(), secret.getDomain());
                Toast.makeText(getApplicationContext(),
                        getString(R.string.toast_copy_secret_website), Toast.LENGTH_SHORT)
                        .show();
                break;
        }

    }
}
