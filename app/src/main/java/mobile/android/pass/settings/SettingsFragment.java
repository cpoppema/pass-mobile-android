package mobile.android.pass.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

import mobile.android.pass.R;


public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private String PREF_KEY_PUBLIC_KEY = "pref_key_public_key";

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource.
        addPreferencesFromResource(R.xml.activity_settings);

        // Load summaries for preferences.
        this.initSummaries();

        // Update summary on change.
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setSummary(findPreference(key), key);
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

        String summary;
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();

        if (key.equals(PREF_KEY_PUBLIC_KEY)) {
            // Calculate key id.
            String publicKey = sharedPreferences.getString(PREF_KEY_PUBLIC_KEY, "");
            if (!publicKey.isEmpty()) {
                summary = "SAMPLEPUBLICKEYID";
            } else {
                summary = "";
            }
        } else {
            summary = sharedPreferences.getString(key, "");
        }

        if(!summary.isEmpty()) {
            preference.setSummary(summary);
        }
    }
}
