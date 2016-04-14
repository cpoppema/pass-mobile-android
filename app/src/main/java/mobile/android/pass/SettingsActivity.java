package mobile.android.pass;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import mobile.android.pass.pgp.PgpHelper;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mShow;
    private EditText mKeyName;
    private EditText mPassword;
    private TextView mPublicKey;

    private PgpHelper mPgpHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mPgpHelper = new PgpHelper(this);

        Button copy = (Button) findViewById(R.id.copy_button);
        Button generate = (Button) findViewById(R.id.generate_button);
        mShow = (Button) findViewById(R.id.show_button);
        mKeyName = (EditText) findViewById(R.id.key_name_input);
        mPassword = (EditText) findViewById(R.id.password_input);
        mPublicKey = (TextView) findViewById(R.id.public_key);

        copy.setOnClickListener(this);
        generate.setOnClickListener(this);
        mShow.setOnClickListener(this);

        updateView();
    }

    private void updateView() {
        mKeyName.setText(mPgpHelper.getPublicKeyName());
        mPassword.getText().clear();
        mPublicKey.setText("");
        mShow.setText("Show");
    }

    private void togglePublicKey() {
        if (mPublicKey.getText().equals("")) {
            mPublicKey.setText(mPgpHelper.getPublicKeyString());
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
                ClipboardHelper.addToClipboard(this, mPgpHelper.getPublicKeyString());
                break;
            case R.id.generate_button:
                // TODO Popup are you sure if key exists.
//                mPgpHelper.generateKeyPair(
//                        mKeyName.getText().toString(),
//                        mPassword.getText().toString()
//                );
//                updateView();
                break;
            case R.id.show_button:
                togglePublicKey();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        finish();
        return true;
    }
}
