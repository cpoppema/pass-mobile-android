package mobile.android.pass.unlock;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import mobile.android.pass.R;
import mobile.android.pass.settings.SettingsActivity;
import mobile.android.pass.utils.StorageHelper;

public class UnlockActivity extends AppCompatActivity {
    private static final String TAG = UnlockActivity.class.toString();

    public static final int NO_DIALOG_TAG = -1;
    public static final int SETTINGS_DIALOG_TAG = 0;
    public static final int UNLOCK_DIALOG_TAG = 1;

    private AlertDialog mDialog;
    private int mDialogTag = NO_DIALOG_TAG;
    private StorageHelper mStorageHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        setContentView(R.layout.activity_unlock);

        mStorageHelper = new StorageHelper(this);

        if (savedInstanceState == null) {
            // Show initial dialog.
            showDialog();
        } else {
            // Restore open dialog if any.
            switch (savedInstanceState.getInt("mDialogTag")) {
                case NO_DIALOG_TAG:
                    // There was no dialog visible.
                    Log.i(TAG, "mDialogTag: NO_DIALOG_TAG");
                    break;
                case SETTINGS_DIALOG_TAG:
                    Log.i(TAG, "mDialogTag: SETTINGS_DIALOG_TAG");
                    showSettingsDialog();
                    break;
                case UNLOCK_DIALOG_TAG:
                    Log.i(TAG, "mDialogTag: UNLOCK_DIALOG_TAG");
                    // This is dealt with in the FragmentManager.
                    break;
            }
        }

        // Show dialog when tapping open area.
        LinearLayoutCompat l = (LinearLayoutCompat) findViewById(R.id.unlock);
        l.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "onSaveInstanceState");

        outState.putInt("mDialogTag", mDialogTag);

        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i(TAG, "onRestoreInstanceState");
        Log.i(TAG, "onRestoreInstanceState - savedInstanceState == null: " + Boolean.toString(savedInstanceState == null));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.i(TAG, "onAttachedToWindow");
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        Log.i(TAG, "onResumeFragments");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_unlock, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.open_settings:
                Log.i(TAG, "Start SettingsActivity");
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setDialogTag(int dialogTag) {
        mDialogTag = dialogTag;
    }

    // Show either unlock or settings dialog.
    private void showDialog() {
        if (mDialogTag == NO_DIALOG_TAG) {
            boolean hasKeypair = !TextUtils.isEmpty(mStorageHelper.getKeyID());
            if (hasKeypair) {
                showUnlockDialog();
            } else {
                showSettingsDialog();
            }
        }
    }

    // Render a new blank unlock dialog, discarding any that already exists.
    private void showUnlockDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("" + UNLOCK_DIALOG_TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        UnlockFragment fragment = new UnlockFragment();
        fragment.show(ft, "" + UNLOCK_DIALOG_TAG);
    }

    private void showSettingsDialog() {
        mDialog = new AlertDialog.Builder(UnlockActivity.this)
                .setTitle(R.string.dialog_settings_title)
                .setMessage(R.string.dialog_settings_message)
                .setPositiveButton(R.string.dialog_settings_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.i(TAG, "Start SettingsActivity");
                        startActivity(new Intent(UnlockActivity.this, SettingsActivity.class));
                    }
                })
                .create();

        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                mDialogTag = NO_DIALOG_TAG;
            }
        });

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                mDialogTag = SETTINGS_DIALOG_TAG;
            }
        });
        
        mDialog.show();
    }
}
