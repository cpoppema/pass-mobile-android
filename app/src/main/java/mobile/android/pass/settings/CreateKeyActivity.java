package mobile.android.pass.settings;

import org.spongycastle.openpgp.PGPSecretKey;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.appcompat.app.AppCompatActivity;

import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Objects;

import mobile.android.pass.R;
import mobile.android.pass.utils.StorageHelper;

/** Shows a form to get input for creating a keypair: a key name and a passphrase. **/

public class CreateKeyActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<PGPSecretKey> {
    private static final String TAG = CreateKeyActivity.class.toString();

    // Set a debug keypair when pressing "Generate 3 times in an empty install"
    private int mEmptyGeneratePressCount = 0;

    // Generate key form view (containing the inputs and button).
    private View mCreateKeyFormView;
    // Progress & cancel key view.
    private View mProgressCancelKeyView;
    // Key name input.
    private EditText mKeyNameView;
    // Passphrase input.
    private EditText mPassphraseView;
    // Create key button.
    private Button mCreateKeyButton;
    // Cancel key button
    private Button mCancelKeyButton;
    // Storage reference.
    private StorageHelper mStorageHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        // Load content from XML resource.
        setContentView(R.layout.activity_generate_key);

        // Add back button to action bar.
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Setup the inputs and button.
        setViews();

        // Pre-fill key name if there is one.
        mStorageHelper = new StorageHelper(this);
        String keyName = mStorageHelper.getKeyName();
        if (!TextUtils.isEmpty(keyName)) {
            mKeyNameView.setText(keyName);
        }

        // Bind button to generate a keypair.
        mCreateKeyButton.setOnClickListener(view -> createKeypair());

        // Bind button to cancel generating a keypair.
        mCancelKeyButton.setOnClickListener(view -> {
            Log.i(TAG, "progressbar dismissed, cancel loader");

            if (LoaderManager.getInstance(CreateKeyActivity.this).hasRunningLoaders()) {
                Objects.requireNonNull(LoaderManager.getInstance(CreateKeyActivity.this).getLoader(0)).cancelLoad();
            }

            showProgress(false);
        });
    }

    private void setViews() {
        mCreateKeyFormView = findViewById(R.id.generate_key_form);
        mProgressCancelKeyView = findViewById(R.id.cancel_generate_key_form);
        mKeyNameView = (EditText) findViewById(R.id.key_name);
        mPassphraseView = (EditText) findViewById(R.id.passphrase);
        mCreateKeyButton = (Button) findViewById(R.id.generate_key_button);
        mCancelKeyButton = (Button) findViewById(R.id.cancel_generate_key_button);

        mKeyNameView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.i(TAG, "onSaveInstanceState");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mEmptyGeneratePressCount = 0;  // always reset counter
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
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(focusView, InputMethodManager.SHOW_IMPLICIT);

            // For debug/review purposes.
            mEmptyGeneratePressCount++;
            if (mEmptyGeneratePressCount >= 3) {
                if (mStorageHelper.getKeyName().isEmpty()) {
                    mStorageHelper.putDebugKeyPair();
                    Toast.makeText(getApplicationContext(), R.string.toast_using_debug_key, Toast.LENGTH_SHORT).show();
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        } else {
            // Save key name.
            mStorageHelper.putKeyName(keyName);

            LoaderManager.getInstance(CreateKeyActivity.this).restartLoader(0, null, this);
        }
    }

    /** Shows or hides the progress dialog and then hides or shows the form. **/
    private void showProgress(final boolean show) {
        // When @show is true show the progress and hide the form otherwise vice versa.
        if (show) {
            // Dismiss keyboard.
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            View viewWithFocus = getCurrentFocus();
            if (viewWithFocus != null) {
                imm.hideSoftInputFromWindow(viewWithFocus.getWindowToken(), 0);
            }

            // Hide generate form.
            mCreateKeyFormView.setVisibility(View.GONE);
            // Show cancel form.
            mProgressCancelKeyView.setVisibility(View.VISIBLE);
        } else {
            // Hide cancel form.
            mProgressCancelKeyView.setVisibility(View.GONE);
            // Show generate form.
            mCreateKeyFormView.setVisibility(View.VISIBLE);
        }
    }

    @NonNull
    @Override
    public Loader<PGPSecretKey> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");

        // Toggle views and create loader.
        showProgress(true);
        return new CreateKeyTaskLoader(this, mKeyNameView.getText().toString(), mPassphraseView.getText().toString());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<PGPSecretKey> loader, PGPSecretKey keyPair) {
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
    public void onLoaderReset(@NonNull Loader<PGPSecretKey> loader) {
        Log.d(TAG, "onLoaderReset");

        // Close activity, returning to the SettingsActivity.
        // TODO: try again ?
        finish();
    }
}

