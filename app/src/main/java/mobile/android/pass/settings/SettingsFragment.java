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


public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private String PREF_KEY_PUBLIC_KEY_ID = "pref_key_public_key_id";

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

//        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString(this.PREF_KEY_PUBLIC_KEY_ID, "50A9895C917E2D3D");
//        editor.commit();

        // Enable interaction for PREF_KEY_PUBLIC_KEY_ID if there is a key.
        Preference keyId = findPreference(this.PREF_KEY_PUBLIC_KEY_ID);
        if(!TextUtils.isEmpty(getPreferenceManager().getSharedPreferences().getString(this.PREF_KEY_PUBLIC_KEY_ID, ""))) {
            keyId.setEnabled(true);
            keyId.setOnPreferenceClickListener(this);
        }

        // Update summary on change.
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
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

        if(TextUtils.equals(key, PREF_KEY_PUBLIC_KEY_ID)) {
            // Enable interaction for PREF_KEY_PUBLIC_KEY_ID if there is a key.
            Preference keyId = findPreference(PREF_KEY_PUBLIC_KEY_ID);
            if(!TextUtils.isEmpty(getPreferenceManager().getSharedPreferences().getString(PREF_KEY_PUBLIC_KEY_ID, ""))) {
                keyId.setEnabled(true);
                keyId.setOnPreferenceClickListener(this);
            } else {
                keyId.setEnabled(false);
                keyId.setOnPreferenceClickListener(null);
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
        if(preference.getKey().equals(PREF_KEY_PUBLIC_KEY_ID)) {
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
                    // TODO: copy key
                    Log.i("pass", "Public key action: " + item.toString());
                    return true;
                case R.id.action_copy_key_id:
                    // TODO: copy key id
                    Log.i("pass", "Public key action: " + item.toString());
                    return true;
                case R.id.action_show_key:
                    // TODO: show key
                    Log.i("pass", "Public key action: " + item.toString());
                    return true;
                default:
                    return super.onContextItemSelected(item);
            }
        }
        return false;
    }
}
