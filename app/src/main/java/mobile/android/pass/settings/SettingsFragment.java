package mobile.android.pass.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import mobile.android.pass.R;
import mobile.android.pass.utils.ClipboardHelper;
import mobile.android.pass.utils.StorageHelper;


public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private static final String TAG = SettingsFragment.class.toString();

    private StorageHelper mStorageHelper;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource.
        addPreferencesFromResource(R.xml.activity_settings);

        // Load summaries for preferences.
        initSummaries();

        // Enable interaction for PREF_KEY_PUBLIC_KEY_ID if there is a key.
        mStorageHelper = new StorageHelper(getActivity());
        String keyId = mStorageHelper.getKeyID();
        Preference keyIdPreference = findPreference(StorageHelper.StorageKey.PUBLIC_KEY_ID.toString());
        if(!TextUtils.isEmpty(keyId)) {
            keyIdPreference.setEnabled(true);
            keyIdPreference.setOnPreferenceClickListener(this);
        }

        // Update summary on change.
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        registerForContextMenu(getView());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_key, menu);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setSummary(findPreference(key), key);

        Log.i(TAG, "Preference changed: " + key);

        if(TextUtils.equals(key, StorageHelper.StorageKey.PUBLIC_KEY_ID.toString())) {
            // Enable interaction for PREF_KEY_PUBLIC_KEY_ID if there is a key.
            Preference keyIdPreference = findPreference(StorageHelper.StorageKey.PUBLIC_KEY_ID.toString());
            String keyId = mStorageHelper.getKeyID();
            if(!TextUtils.isEmpty(keyId)) {
                keyIdPreference.setEnabled(true);
                keyIdPreference.setOnPreferenceClickListener(this);
            } else {
                keyIdPreference.setEnabled(false);
                keyIdPreference.setOnPreferenceClickListener(null);
            }
        }
    }

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

    private void setSummary(Preference preference, String key) {
        if(key == null || preference == null) {
            return;
        }

        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        String summary = sharedPreferences.getString(key, "");
        if(!summary.isEmpty()) {
            preference.setSummary(summary);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if(preference.getKey().equals(StorageHelper.StorageKey.PUBLIC_KEY_ID.toString())) {
            getActivity().openContextMenu(getView());
            return true;
        }
        return false;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(getUserVisibleHint()) {
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
                    // TODO: show key
                    Log.i(TAG, "Public key action: " + item.toString());
                    String keyToShow = mStorageHelper.getArmoredPublicKey();
                    Log.i(TAG, "Public key: " + keyToShow);
                    return true;
                default:
                    return super.onContextItemSelected(item);
            }
        }
        return false;
    }
}
