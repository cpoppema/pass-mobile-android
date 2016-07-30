package mobile.android.pass.settings;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import mobile.android.pass.R;
import mobile.android.pass.utils.ClipboardHelper;
import mobile.android.pass.utils.StorageHelper;


public class SettingsFragmentRevised extends Fragment
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        View.OnClickListener {
    private static final String TAG = SettingsFragmentRevised.class.toString();

    // Reference to preferences.
    private SharedPreferences mSharedPreferences;
    // Storage reference.
    private StorageHelper mStorageHelper;
    // Restore/save ContextMenu from/to this state.
    private boolean mContextMenuOpen = false;
    // Reference to the Preference with the ContextMenu.
    private View mKeyIdPreference;

    public SettingsFragmentRevised() {
        // Required empty public constructor.
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        // Instantiate custom storage interface.
        mStorageHelper = new StorageHelper(getActivity());

        // Get references through activity.
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Instead of setContentView, inflate here to trigger a view to be created so getView()
        // actually returns something and we can register a ContextMenu for it.
        return inflater.inflate(R.layout.fragment_settings, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.i(TAG, "onViewCreated");

        // Load summaries for preferences.
        initSummaries();
        setEnabledStateForKeyID();

        // Update summaries on change.
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

        // getView() returns null in onCreate/onCreateView, so do this in onViewCreated.
        registerForContextMenu(view);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.i(TAG, "onSaveInstanceState");

        // Remember if a ContextMenu was visible.
        outState.putBoolean("mContextMenuOpen", mContextMenuOpen);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        Log.i(TAG, "onViewStateRestored");

        if (savedInstanceState != null) {
            // Restore ContextMenu's visibility.
            mContextMenuOpen = savedInstanceState.getBoolean("mContextMenuOpen");
            if (mContextMenuOpen) {
                getView().post(new Runnable() {
                    @Override
                    public void run() {
                        showContextMenu();
                    }
                });
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");

        // These listeners will stack, so unregister.
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    private void showContextMenu() {
        mContextMenuOpen = true;

        getActivity().openContextMenu(getView());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        Log.i(TAG, "onCreateContextMenu");

        // Inflate from XML resource.
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_key, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        mContextMenuOpen = false;

        switch (item.getItemId()) {
            case R.id.action_copy_key:
                Log.i(TAG, "Public key action: " + item.toString());
                String keyToCopy = mStorageHelper.getArmoredPublicKey();
                Log.i(TAG, "Public key: " + keyToCopy);
                ClipboardHelper.copy(getActivity(), keyToCopy);
                return true;
            case R.id.action_copy_key_id:
                Log.i(TAG, "Public key action: " + item.toString());
                String keyID = mStorageHelper.getKeyID();
                Log.i(TAG, "Key ID: " + keyID);
                ClipboardHelper.copy(getActivity(), keyID);
                return true;
            case R.id.action_show_key:
                startActivity(new Intent(getActivity(), PublicKeyActivity.class));
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void onContextMenuClosed(Menu menu) {
        Log.i(TAG, "onContextMenuClosed");
        mContextMenuOpen = false;
    }

    /** Enables/disables interaction for @mKeyIdPreference if a local key exists. **/
    private void setEnabledStateForKeyID() {
        if (mKeyIdPreference == null) {
            mKeyIdPreference = getActivity().findViewById(R.id.pref_key_public_key);
        }

        if (TextUtils.isEmpty(mStorageHelper.getKeyID())) {
            // Disable interaction.
            mKeyIdPreference.setEnabled(false);
            mKeyIdPreference.setOnClickListener(null);
        } else {
            // Enable interaction.
            mKeyIdPreference.setEnabled(true);
            mKeyIdPreference.setOnClickListener(this);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setSummary(key);

        Log.i(TAG, "Preference changed: " + key);

        if (TextUtils.equals(key, StorageHelper.StorageKey.PUBLIC_KEY_ID.toString())) {
            setEnabledStateForKeyID();
        }
    }

    /** Loops through all preferences in the PreferenceScreen and set its initial summary. **/
    private void initSummaries() {
        setSummary(StorageHelper.StorageKey.SERVER_ADDRESS.toString());
        setSummary(StorageHelper.StorageKey.PUBLIC_KEY_NAME.toString());
        setSummary(StorageHelper.StorageKey.PUBLIC_KEY_ID.toString());
    }

    /** Sets summary for a single Preference. **/
    private void setSummary(String key) {
        if (key == null) {
            return;
        }

        String summary = mSharedPreferences.getString(key, "");
        if (!summary.isEmpty()) {
            TextView textView = null;
            if (key == StorageHelper.StorageKey.SERVER_ADDRESS.toString()) {
                textView = (TextView) getActivity().findViewById(R.id.summary_server);
            } else if (key == StorageHelper.StorageKey.PUBLIC_KEY_NAME.toString()) {
                textView = (TextView) getActivity().findViewById(R.id.summary_key_name);
            } else if (key == StorageHelper.StorageKey.PUBLIC_KEY_ID.toString()) {
                textView = (TextView) getActivity().findViewById(R.id.summary_public_key);
            }

            if (textView != null) {
                textView.setText(summary);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pref_key_public_key:
                showContextMenu();
                break;
            default:
                break;
        }
    }
}
