package mobile.android.pass.settings;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;
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

/**
 * A login screen that offers login via email/password.
 */
public class GenerateKeyActivity extends AppCompatActivity {

    private String PREF_KEY_KEY_NAME = "pref_key_key_name";

    /**
     * Keep track of the createKeypair task to ensure we can cancel it if requested.
     */
    private CreateKeypairTask mCreateKeypairTask = null;

    // UI references.
    private EditText mKeyNameView;
    private EditText mPassphraseView;
    private ProgressDialog mProgressDialog;
    private View mCreateKeyFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_key);
        // Set up the create key form.
        mKeyNameView = (EditText) findViewById(R.id.key_name);
//        getLoaderManager().initLoader(0, null, this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String keyName = sharedPreferences.getString(this.PREF_KEY_KEY_NAME, "");
        if (!keyName.isEmpty()) {
            mKeyNameView.setText(keyName);
        }

        mPassphraseView = (EditText) findViewById(R.id.passphrase);

        Button mCreateKeyButton = (Button) findViewById(R.id.generate_key_button);
        mCreateKeyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                createKeypair();
            }
        });

        mCreateKeyFormView = findViewById(R.id.generate_key_form);

        // Add back button to action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        if (mCreateKeypairTask != null) {
            return;
        }

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

            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
//            showProgress(true);
            mCreateKeypairTask = new CreateKeypairTask(keyName, passphrase);
            mCreateKeypairTask.execute((Void) null);
        }
    }

//    private boolean isEmailValid(String email) {
//        //TODO: Replace this with your own logic
//        return email.contains("@");
//    }
//
//    private boolean isPasswordValid(String password) {
//        //TODO: Replace this with your own logic
//        return password.length() > 4;
//    }

    /**
     * Shows the progress UI and hides the login form.
     */
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
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

            // Hide form.
            mCreateKeyFormView.setVisibility(View.GONE);

            // Show progress dialog.
            mProgressDialog.show();
        } else {
            mProgressDialog.dismiss();
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class CreateKeypairTask extends AsyncTask<Void, Void, Boolean> {

        private final String mKeyName;
        private final String mPassword;

        public CreateKeypairTask(String keyName, String passphrase) {
            mKeyName = keyName;
            mPassword = passphrase;
        }

        @Override
        protected void onPreExecute() {
            showProgress(true);

            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mCreateKeypairTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                // TODO: is this reachable code ?
                Log.d("pass", "creating keypair failed");
            }
        }

        @Override
        protected void onCancelled() {
            mCreateKeypairTask = null;
            showProgress(false);
        }
    }
}

