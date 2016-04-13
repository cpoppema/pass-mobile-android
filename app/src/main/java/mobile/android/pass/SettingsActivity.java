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
    private Button mCopy;
    private Button mGenerate;
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

        mCopy = (Button) findViewById(R.id.copy_button);
        mGenerate = (Button) findViewById(R.id.generate_button);
        mKeyName = (EditText) findViewById(R.id.key_name_input);
        mPassword = (EditText) findViewById(R.id.password_input);
        mPublicKey = (TextView) findViewById(R.id.public_key);

        mCopy.setOnClickListener(this);
        mGenerate.setOnClickListener(this);

        updateView();
    }

    private void updateView() {
        mKeyName.setText(mPgpHelper.getPublicKeyName());
        mPassword.getText().clear();
        mPublicKey.setText(mPgpHelper.getPublicKeyString());
    }

    @Override
    public void onClick(View v) {
        // TODO generate and copy.
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        finish();
        return true;
    }
}
