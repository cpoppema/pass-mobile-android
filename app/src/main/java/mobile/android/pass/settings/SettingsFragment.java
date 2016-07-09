package mobile.android.pass.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import mobile.android.pass.R;
import mobile.android.pass.utils.ClipboardHelper;
import mobile.android.pass.utils.StorageHelper;

/** Shows a list of (im)mutable preferences and key information. **/

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceClickListener {
    private static final String TAG = SettingsFragment.class.toString();

    // Storage reference.
    private StorageHelper mStorageHelper;
    // Restore/save ContextMenu from/to this state.
    private boolean mContextMenuOpen = false;
    // Reference to the Preference with the ContextMenu.
    private Preference mKeyIdPreference;

    public SettingsFragment() {
        // Required empty public constructor.
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        // TODO: Is this necessary ? Should make SettingsActivity.onCreate easier since there won't ever be anything on the BackStack.
        // Retain this fragment's state when config changes.
        setRetainInstance(true);

        // Instantiate custom storage interface.
        mStorageHelper = new StorageHelper(getActivity());

        // Load the preferences from an XML resource.
        addPreferencesFromResource(R.xml.activity_settings);

        // Load summaries for preferences.
        initSummaries();
        setEnabledStateForKeyID();

        // Update summaries on change.
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.i(TAG, "onViewCreated");

        // getView() returns null in onCreate, so do this in onViewCreated.
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
    public void onResume() {
        super.onResume();

        Log.i(TAG, "onResume");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");

        // FIXME: Close open EditTextPreference's Dialog
        // NOTE: Symptoms (besides the exception): having to close multiple PreferenceDialog's that don't have an EditText any more.
        // NOTE: setRetainInstance(false) doesn't make a difference
        // https://code.google.com/p/android/issues/detail?id=185211
        // https://code.google.com/p/android/issues/detail?id=186160

        // These listeners will stack, so unregister.
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private void showContextMenu() {
        mContextMenuOpen = true;

        getActivity().openContextMenu(getView());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        Log.i(TAG, "onActivityCreated");

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
            mKeyIdPreference = findPreference(StorageHelper.StorageKey.PUBLIC_KEY_ID.toString());
        }

        if (TextUtils.isEmpty(mStorageHelper.getKeyID())) {
            // Disable interaction.
            mKeyIdPreference.setEnabled(false);
            mKeyIdPreference.setOnPreferenceClickListener(null);
        } else {
            // Enable interaction.
            mKeyIdPreference.setEnabled(true);
            mKeyIdPreference.setOnPreferenceClickListener(this);
        }
    }



    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setSummary(findPreference(key), key);

        Log.i(TAG, "Preference changed: " + key);

        if (TextUtils.equals(key, StorageHelper.StorageKey.PUBLIC_KEY_ID.toString())) {
            setEnabledStateForKeyID();
        }
    }

    /** Loops through all preferences in the PreferenceScreen and set its initial summary. **/
    private void initSummaries() {
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); ++i) {
            Preference preference = getPreferenceScreen().getPreference(i);
            if (preference instanceof PreferenceGroup) {
                PreferenceGroup preferenceGroup = (PreferenceGroup) preference;
                for (int j = 0; j < preferenceGroup.getPreferenceCount(); ++j) {
                    Preference singlePref = preferenceGroup.getPreference(j);
                    setSummary(singlePref, singlePref.getKey());
                }
            } else {
                setSummary(preference, preference.getKey());
            }
        }
    }

    /** Sets summary for a single Preference. **/
    private void setSummary(Preference preference, String key) {
        if (key == null || preference == null) {
            return;
        }

        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        String summary = sharedPreferences.getString(key, "");
        if (!summary.isEmpty()) {
            preference.setSummary(summary);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(StorageHelper.StorageKey.PUBLIC_KEY_ID.toString())) {
            showContextMenu();
            return true;
        }
        return false;
    }
}
