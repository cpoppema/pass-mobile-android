package mobile.android.pass.settings;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import mobile.android.pass.R;

/**
 * A login screen that offers login via email/password.
 */
public class CreateKeyActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private String PREF_KEY_KEY_NAME = "pref_key_key_name";

    /**
     * Keep track of the createKeypair task to ensure we can cancel it if requested.
     */
    private CreateKeypairTask mCreateKeypairTask = null;

    // UI references.
    private EditText mKeyNameView;
    private EditText mPassphraseView;
    private View mProgressView;
    private View mCreateKeyFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_key);
        // Set up the create key form.
        mKeyNameView = (EditText) findViewById(R.id.key_name);
//        getLoaderManager().initLoader(0, null, this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String keyName = sharedPreferences.getString(this.PREF_KEY_KEY_NAME, "");
        if (!keyName.isEmpty()) {
            mKeyNameView.setText(keyName);
        }

        mPassphraseView = (EditText) findViewById(R.id.passphrase);
//        mPassphraseView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
//                if (id == R.id.login || id == EditorInfo.IME_NULL) {
//                    attemptLogin();
//                    return true;
//                }
//                return false;
//            }
//        });

        Button mCreateKeyButton = (Button) findViewById(R.id.create_key_button);
        mCreateKeyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                createKeypair();
            }
        });

        mCreateKeyFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

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
            showProgress(true);
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
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mCreateKeyFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mCreateKeyFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCreateKeyFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mCreateKeyFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this);
//        return new CursorLoader(this,
//                // Retrieve data rows for the device user's 'profile' contact.
//                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
//                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,
//
//                // Select only email addresses.
//                ContactsContract.Contacts.Data.MIMETYPE +
//                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
//                .CONTENT_ITEM_TYPE},
//
//                // Show primary email addresses first. Note that there won't be
//                // a primary email address if the user hasn't specified one.
//                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

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
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

//            for (String credential : DUMMY_CREDENTIALS) {
//                String[] pieces = credential.split(":");
//                if (pieces[0].equals(mKeyName)) {
//                    // Account exists, return true if the password matches.
//                    return pieces[1].equals(mPassword);
//                }
//            }

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
//                mPassphraseView.setError(getString(R.string.error_incorrect_password));
//                mPassphraseView.requestFocus();
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

