package mobile.android.pass.settings;

import org.spongycastle.openpgp.PGPSecretKey;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import mobile.android.pass.R;
import mobile.android.pass.utils.StorageHelper;

/** Shows a form to get input for creating a keypair: a key name and a passphrase. **/

public class CreateKeyActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<PGPSecretKey> {
    private static final String TAG = CreateKeyActivity.class.toString();

    // Form view (containing the inputs and button).
    private View mCreateKeyFormView;
    // Key name input.
    private EditText mKeyNameView;
    // Passphrase input.
    private EditText mPassphraseView;
    // Create key button.
    private Button mCreateKeyButton;
    // Dialog to show while generating the keypair.
    private ProgressDialog mProgressDialog;
    // Storage reference.
    private StorageHelper mStorageHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        // Load content from XML resource.
        setContentView(R.layout.activity_generate_key);

        // Add back button to action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Setup the inputs and button.
        setViews();

        // Pre-fill key name if there is one.
        mStorageHelper = new StorageHelper(this);
        String keyName = mStorageHelper.getKeyName();
        if (!TextUtils.isEmpty(keyName)) {
            mKeyNameView.setText(keyName);
        }

        // Bind button to generate a keypair.
        mCreateKeyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                createKeypair();
            }
        });
    }

    private void setViews() {
        mCreateKeyFormView = findViewById(R.id.generate_key_form);
        mKeyNameView = (EditText) findViewById(R.id.key_name);
        mPassphraseView = (EditText) findViewById(R.id.passphrase);
        mCreateKeyButton = (Button) findViewById(R.id.generate_key_button);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.i(TAG, "onSaveInstanceState");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Log.i(TAG, "onRestoreInstanceState");
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

        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            // Close it to prevent leaking it.
            mProgressDialog.dismiss();
        }
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** Validates input and creates a loader when it's valid. **/
    private void createKeypair() {
        Log.i(TAG, "createKeypair");

        // Reset errors.
        mKeyNameView.setError(null);
        mPassphraseView.setError(null);

        // Store values at the time of the login attempt.
        String keyName = mKeyNameView.getText().toString();
        String passphrase = mPassphraseView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid passphrase.
        if (TextUtils.isEmpty(passphrase)) {
            mPassphraseView.setError(getString(R.string.error_field_required));
            focusView = mPassphraseView;
            cancel = true;
            Log.i(TAG, "cancel: passphrase");
        }

        // Check for a valid key name.
        if (TextUtils.isEmpty(keyName)) {
            mKeyNameView.setError(getString(R.string.error_field_required));
            focusView = mKeyNameView;
            cancel = true;
            Log.i(TAG, "cancel: keyName");
        }
        if (cancel) {
            // There was an error; don't attempt anything and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Save key name.
            mStorageHelper.putKeyName(keyName);

            getSupportLoaderManager().restartLoader(0, null, this);
        }
    }

    /** Shows or hides the progress dialog and then hides or shows the form. **/
    private void showProgress(final boolean show) {
        if (mProgressDialog == null) {
            // Setup the ProgressDialog for the first time.
            mProgressDialog = new ProgressDialog(this, R.style.AppTheme_ProgressDialogTextView);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMessage(getString(R.string.progress_generating_key));

            // Don't dismiss on touch outside (back button still works).
            mProgressDialog.setCanceledOnTouchOutside(false);

            // Cancel task and toggle views when dialog is dismissed.
            mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    Log.i(TAG, "progress dialog dismissed, cancel loader");

                    if (getSupportLoaderManager().hasRunningLoaders()) {
                        getSupportLoaderManager().getLoader(0).cancelLoad();
                    }

                    showProgress(false);
                }
            });
        }

        // When @show is true show the progress and hide the form otherwise vice versa.
        if (show) {
            // Dismiss keyboard.
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            View viewWithFocus = getCurrentFocus();
            if (viewWithFocus != null) {
                imm.hideSoftInputFromWindow(viewWithFocus.getWindowToken(), 0);
            }

            // Hide form.
            mCreateKeyFormView.setVisibility(View.GONE);

            // Show progress dialog.
            mProgressDialog.show();
        } else {
            // Hide progress dialog.
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }

            // Show form.
            mCreateKeyFormView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public Loader<PGPSecretKey> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");

        // Toggle views and create loader.
        showProgress(true);
        return new CreateKeyTaskLoader(this, mKeyNameView.getText().toString(), mPassphraseView.getText().toString());
    }

    @Override
    public void onLoadFinished(Loader<PGPSecretKey> loader, PGPSecretKey keyPair) {
        Log.d(TAG, "onLoadFinished");

        if (keyPair == null) {
            showProgress(false);
            // TODO: No result but this isn't a cancellation by user. Try again ? automatically ?
        } else {
            // Save key data.
            mStorageHelper.putKeyPair(keyPair);

            // Close activity, returning to the SettingsActivity.
            finish();
        }
    }

    @Override
    public void onLoaderReset(Loader<PGPSecretKey> loader) {
        Log.d(TAG, "onLoaderReset");

        // Close activity, returning to the SettingsActivity.
        // TODO: try again ?
        finish();
    }
}

