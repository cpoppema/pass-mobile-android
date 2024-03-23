package mobile.android.pass.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import java.util.Objects;

import mobile.android.pass.BuildConfig;
import mobile.android.pass.R;
import mobile.android.pass.utils.ClipboardHelper;
import mobile.android.pass.utils.StorageHelper;

/** Activity showing current settings. **/

public class SettingsActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        View.OnClickListener {
    // Indicates there is no popup visible.
    public static final int NO_DIALOG_TAG = -1;
    // Indicates the server name dialog is visible.
    public static final int SERVER_NAME_DIALOG_TAG = 1;

    private static final String TAG = SettingsActivity.class.toString();

    // Reference to preferences.
    private SharedPreferences mSharedPreferences;
    // Storage reference.
    private StorageHelper mStorageHelper;
    // Restore/save ContextMenu from/to this state.
    private boolean mContextMenuOpen = false;
    // Restore/save the right dialog state based on this tag.
    private int mDialogTag = NO_DIALOG_TAG;

    // Reference to the parent view in the xml.
    private View mSettingsView;
    // Reference to the server name (listen to click for edit dialog).
    private View mServerNamePreference;
    // Reference to the public key (listen to click for context menu).
    private View mKeyIdPreference;
    // Reference to text views.
    private TextView mServerNameText;
    private TextView mKeyNameText;
    private TextView mKeyIdText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        // Instantiate custom storage interface.
        mStorageHelper = new StorageHelper(this);

        // Get references through activity.
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Add back button to action bar.
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_settings);
        mSettingsView = findViewById(R.id.settings);
        registerForContextMenu(mSettingsView);
        mServerNamePreference = findViewById(R.id.pref_key_server_name);
        mServerNamePreference.setOnClickListener(this);
        mKeyIdPreference = findViewById(R.id.pref_key_public_key);
        setEnabledStateForKeyID();
        mServerNameText = (TextView) findViewById(R.id.summary_server);
        mKeyNameText = (TextView) findViewById(R.id.summary_key_name);
        mKeyIdText = (TextView) findViewById(R.id.summary_public_key);

        // Load summaries.
        initSummaries();

        // Update summaries on change.
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

        if (savedInstanceState != null) {
            // Restore open dialog if any.
            if (mDialogTag == NO_DIALOG_TAG) {
                // There was no dialog visible.
                Log.i(TAG, "mDialogTag: NO_DIALOG_TAG");
            } else if (mDialogTag == SERVER_NAME_DIALOG_TAG) {
                Log.i(TAG, "mDialogTag: SERVER_NAME_DIALOG_TAG");
                // This is dealt with in the FragmentManager.
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.i(TAG, "onSaveInstanceState");

        // Remember if a ContextMenu was visible.
        outState.putBoolean("mContextMenuOpen", mContextMenuOpen);

        // Remember open dialog state.
        outState.putInt("mDialogTag", mDialogTag);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Log.i(TAG, "onRestoreInstanceState");

        if (savedInstanceState != null) {
            // Restore ContextMenu's visibility.
            mContextMenuOpen = savedInstanceState.getBoolean("mContextMenuOpen");
            if (mContextMenuOpen) {
                mSettingsView.post(this::showContextMenu);
            }

            // Restore open dialog state.
            mDialogTag = savedInstanceState.getInt("mDialogTag");
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

        openContextMenu(mSettingsView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        Log.i(TAG, "onCreateContextMenu");

        // Inflate from XML resource.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_key, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        mContextMenuOpen = false;

        int itemId = item.getItemId();
        if (itemId == R.id.action_copy_key) {
            Log.i(TAG, "Public key action: " + item);
            String keyToCopy = mStorageHelper.getArmoredPublicKey();
            Log.i(TAG, "Public key: " + keyToCopy);
            ClipboardHelper.copy(this, keyToCopy);
            Toast.makeText(getApplicationContext(),
                            getString(R.string.toast_copy_key), Toast.LENGTH_SHORT)
                    .show();
            return true;
        } else if (itemId == R.id.action_copy_key_id) {
            Log.i(TAG, "Public key action: " + item);
            String keyID = mStorageHelper.getKeyID();
            Log.i(TAG, "Key ID: " + keyID);
            ClipboardHelper.copy(this, keyID);
            Toast.makeText(getApplicationContext(),
                            getString(R.string.toast_copy_key_id), Toast.LENGTH_SHORT)
                    .show();
            return true;
        } else if (itemId == R.id.action_show_key) {
            startActivity(new Intent(this, PublicKeyActivity.class));
            return true;
        }
        return super.onContextItemSelected(item);
    }

    public void onContextMenuClosed(Menu menu) {
        super.onContextMenuClosed(menu);

        Log.i(TAG, "onContextMenuClosed");
        mContextMenuOpen = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate from XML resource.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_settings, menu);

        return true;
    }

    /** Enables/disables interaction for @mKeyIdPreference if a local key exists. **/
    private void setEnabledStateForKeyID() {
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
        setPreferenceSummary(key);

        Log.i(TAG, "Preference changed: " + key);

        if (TextUtils.equals(key, StorageHelper.StorageKey.PUBLIC_KEY_ID.toString())) {
            setEnabledStateForKeyID();
        }
    }

    private void initSummaries() {
        setPreferenceSummary(StorageHelper.StorageKey.SERVER_ADDRESS.toString());
        setPreferenceSummary(StorageHelper.StorageKey.PUBLIC_KEY_NAME.toString());
        setPreferenceSummary(StorageHelper.StorageKey.PUBLIC_KEY_ID.toString());

        TextView textView = (TextView) findViewById(R.id.summary_version);
        textView.setText(BuildConfig.VERSION_NAME);
    }

    /** Sets summary for a single Preference. **/
    private void setPreferenceSummary(String key) {
        if (key == null) {
            return;
        }

        String summary = mSharedPreferences.getString(key, "");
        if (!summary.isEmpty()) {
            if (key.equals(StorageHelper.StorageKey.SERVER_ADDRESS.toString())) {
                mServerNameText.setText(summary);
            } else if (key.equals(StorageHelper.StorageKey.PUBLIC_KEY_NAME.toString())) {
                mKeyNameText.setText(summary);
            } else if (key.equals(StorageHelper.StorageKey.PUBLIC_KEY_ID.toString())) {
                mKeyIdText.setText(summary);
            }
        }
    }

    /** Sets the current open dialog state. **/
    public void setDialogTag(int dialogTag) {
        mDialogTag = dialogTag;
    }

    /** Render a dialog for editing the server address. **/
    private void showServerNameDialog() {
        ServerNameFragment fragment = new ServerNameFragment();
        fragment.show(getSupportFragmentManager(), "" + SERVER_NAME_DIALOG_TAG);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.pref_key_server_name) {
            showServerNameDialog();
        } else if (id == R.id.pref_key_public_key) {
            showContextMenu();
        } else {
            Log.i(TAG, "Clicked on " + v.getId());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        } else if (itemId == R.id.open_generate_key) {
            Log.i(TAG, "Start CreateKeyActivity");
            startActivity(new Intent(this, CreateKeyActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}