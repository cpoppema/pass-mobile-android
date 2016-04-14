package mobile.android.pass.settings;

import android.content.DialogInterface;
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

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mShow;
    private EditText mKeyName;
    private EditText mPassword;
    private EditText mServerAddress;
    private TextView mPublicKey;

    private PgpHelper mPgpHelper;
    private Storage mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mPgpHelper = new PgpHelper(this);
        mStorage = new Storage(this);

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

        updateView();
    }

    private void updateView() {
        mServerAddress.setText(mStorage.getServerAddress());
        mKeyName.setText(mStorage.getPublicKeyName());
        mPassword.getText().clear();
        mPublicKey.setText("");
        mShow.setText("Show");
    }

    private void togglePublicKey() {
        if (mPublicKey.getText().equals("")) {
            mPublicKey.setText(mStorage.getPublicKey());
            mShow.setText("Hide");
        } else {
            mPublicKey.setText("");
            mShow.setText("Show");
        }
    }

    @Override
    public void onClick(View view) {
        // TODO generate and copy.
        switch (view.getId()) {
            case R.id.copy_button:
                ClipboardHelper.addToClipboard(this, mStorage.getPublicKey());
                Toast.makeText(this, "Password copied to clipboard", Toast.LENGTH_SHORT).show();
                break;
            case R.id.generate_button:
                // TODO Popup are you sure if key exists.
                if (mStorage.hasKeyPair()) {
                    showConfirmationDialog();
                }
                break;
            case R.id.show_button:
                togglePublicKey();
                break;
            case R.id.server_address_button:
                mStorage.setServerAddress(mServerAddress.getText().toString());
                break;
        }
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Caution!");
        builder.setMessage("Generating a keypair wil override existing keypair. Are you sure you" +
                "want to do this?");

        builder.setPositiveButton("Yes, override", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPgpHelper.generateKeyPair(
                        mKeyName.getText().toString(),
                        mPassword.getText().toString()
                );
                updateView();
                Toast.makeText(SettingsActivity.this, "New keypair generated", Toast.LENGTH_SHORT).show();
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
}
