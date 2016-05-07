package mobile.android.pass.settings;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import mobile.android.pass.R;
import mobile.android.pass.secrets.SecretsActivity;

public class CreateKeyActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Object> {

    private static final String TAG = SecretsActivity.class.toString();

    private String PREF_KEY_KEY_NAME = "pref_key_key_name";

    private EditText mKeyNameView;
    private EditText mPassphraseView;
    private ProgressDialog mProgressDialog;
    private View mCreateKeyFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_key);

        // Add back button to action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mKeyNameView = (EditText) findViewById(R.id.key_name);
        mPassphraseView = (EditText) findViewById(R.id.passphrase);
        mCreateKeyFormView = findViewById(R.id.generate_key_form);

        // Pre-fill key name if there is one.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String keyName = sharedPreferences.getString(this.PREF_KEY_KEY_NAME, "");
        if (!keyName.isEmpty()) {
            mKeyNameView.setText(keyName);
        }

        // Bind button to generate a keypair.
        Button mCreateKeyButton = (Button) findViewById(R.id.generate_key_button);
        mCreateKeyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                createKeypair();
            }
        });
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

    private void createKeypair() {
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
        }

        // Check for a valid key name.
        if (TextUtils.isEmpty(keyName)) {
            mKeyNameView.setError(getString(R.string.error_field_required));
            focusView = mKeyNameView;
            cancel = true;
        }
        if (cancel) {
            // There was an error; don't attempt anything and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Save key name.
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(this.PREF_KEY_KEY_NAME, keyName);
            editor.commit();

            getSupportLoaderManager().initLoader(0, null, this);
        }
    }

    private void showProgress(final boolean show) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMessage(getString(R.string.progress_generating_key));
            mProgressDialog.create();
        }

        if (show) {
            // Dismiss keyboard.
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

            // Hide form.
            mCreateKeyFormView.setVisibility(View.GONE);

            // Show progress dialog.
            mProgressDialog.show();
        } else {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");
        showProgress(true);
        return new CreateKeyTaskLoader(this, mKeyNameView.getText().toString(), mPassphraseView.getText().toString());
    }

    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {
        Log.d(TAG, "onLoadFinished");


        if (data == null) {
            showProgress(false);
            // TODO: Try again ?
        } else {
            // Close activity and go back.
            Log.d(TAG, "finish()");
            mProgressDialog.dismiss();
            finish();
        }
    }

    @Override
    public void onLoaderReset(Loader<Object> loader) {
        Log.d(TAG, "onLoaderReset");

        // TODO: try again ?
        finish();
    }
}

