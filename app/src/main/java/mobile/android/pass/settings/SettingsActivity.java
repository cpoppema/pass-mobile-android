package mobile.android.pass.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import mobile.android.pass.R;

/** Activity that shows the SettingsFragment. **/

public class SettingsActivity extends AppCompatActivity {
    public static final int SETTINGS_DIALOG_TAG = 1;

    private static final String TAG = SettingsActivity.class.toString();

    // Fragment reference.
    private SettingsFragmentRevised mSettingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        // Attempt to get a previously created SettingsFragment reference.
        mSettingsFragment = (SettingsFragmentRevised) getFragmentManager().findFragmentByTag("" + SETTINGS_DIALOG_TAG);
        // Non-null means it is being retained, no need to create it again.
        if (mSettingsFragment == null) {
            mSettingsFragment = new SettingsFragmentRevised();

            // Display the fragment as the main content.
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, mSettingsFragment, "" + SETTINGS_DIALOG_TAG)
                    .commit();
        }

        // Add back button to action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate from XML resource.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_settings, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.open_generate_key:
                Log.i(TAG, "Start CreateKeyActivity");
                startActivity(new Intent(this, CreateKeyActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onContextMenuClosed(Menu menu) {
        Log.i(TAG, "onContextMenuClosed");

        super.onContextMenuClosed(menu);

        // Act as a intermediary so the fragment can track the visibility state of the ContextMenu.
        if (mSettingsFragment != null) {
            mSettingsFragment.onContextMenuClosed(menu);
        }
    }
}