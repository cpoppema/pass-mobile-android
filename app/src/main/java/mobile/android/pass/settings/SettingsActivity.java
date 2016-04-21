package mobile.android.pass.settings;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import mobile.android.pass.R;
import mobile.android.pass.pgp.PgpHelper;
import mobile.android.pass.utils.ClipboardHelper;
import mobile.android.pass.utils.Storage;

/**
 * Activity that shows the settings page.
 */
public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mShow;
    private EditText mKeyName;
    private EditText mPassword;
    private EditText mServerAddress;
    private ProgressDialog mProgressDialog;
    private TextView mPublicKey;

    private PgpHelper mPgpHelper;
    private Storage mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set the back button on the settings page.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mPgpHelper = new PgpHelper(this);
        mStorage = new Storage(this);

        initViewsAndListeners();
        updateView();
    }

    /**
     * Function to get all views and init click listeners on the buttons.
     */
    private void initViewsAndListeners() {
        Button copy = (Button) findViewById(R.id.copy_button);
        Button generate = (Button) findViewById(R.id.generate_button);
        Button updateServer = (Button) findViewById(R.id.server_address_button);
        mShow = (Button) findViewById(R.id.show_button);
        mKeyName = (EditText) findViewById(R.id.key_name_input);
        mPassword = (EditText) findViewById(R.id.password_input);
        mServerAddress = (EditText) findViewById(R.id.server_address_input);
        mPublicKey = (TextView) findViewById(R.id.public_key);

        copy.setOnClickListener(this);
        generate.setOnClickListener(this);
        updateServer.setOnClickListener(this);
        mShow.setOnClickListener(this);
    }

    /**
     * Function to update the view to reflect data changes.
     */
    private void updateView() {
        mServerAddress.setText(mStorage.getServerAddress());
        mKeyName.setText(mStorage.getPublicKeyName());
        mPassword.getText().clear();
        mPublicKey.setVisibility(View.GONE);
        mPublicKey.setText(mStorage.getPublicKey());
        mShow.setText("Show");
    }

    /**
     * Function to toggle the view of the public key.
     */
    private void togglePublicKey() {
        mShow.setText(mPublicKey.getVisibility() == View.VISIBLE ? "Hide" : "show");
        mPublicKey.setVisibility(mPublicKey.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.copy_button:
                ClipboardHelper.addToClipboard(this, mStorage.getPublicKey());
                Toast.makeText(this, "Public key copied to clipboard", Toast.LENGTH_SHORT).show();
                break;
            case R.id.generate_button:
                // Show confirmation dialog when a keypair exist because it will be overridden.
                if (mStorage.hasKeyPair()) {
                    showConfirmationDialog();
                } else {
                    new generateKeyPairTask().execute(mKeyName.getText().toString(),
                            mPassword.getText().toString());
                }
                break;
            case R.id.show_button:
                togglePublicKey();
                break;
            case R.id.server_address_button:
                // TODO Feedback on server address sanity.
                mStorage.setServerAddress(mServerAddress.getText().toString());
                Toast.makeText(this, "Server address updated", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * Function to show a confirmation dialog for generating a new keypair.
     */
    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Caution!");
        builder.setMessage("Generating a keypair wil override existing keypair. Are you sure you" +
                "want to do this?");

        builder.setPositiveButton("Yes, override", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new generateKeyPairTask().execute(mKeyName.getText().toString(),
                        mPassword.getText().toString());
            }
        });
        builder.setNegativeButton("No, back to safety", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        finish();
        return true;
    }

    /**
     * Function to setup the progress dialog for creating a key pair.
     */
    private void createProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage("Generating key pair");
        mProgressDialog.create();
    }

    private class generateKeyPairTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            createProgressDialog();
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            String keyName = params[0];
            String password = params[1];

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mPgpHelper.generateKeyPair(keyName, password);

            keyName = null;
            password = null;

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mProgressDialog.cancel();
            updateView();
            Toast.makeText(SettingsActivity.this, "New keypair generated", Toast.LENGTH_SHORT).show();
        }
    }


}
