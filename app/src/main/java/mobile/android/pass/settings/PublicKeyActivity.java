package mobile.android.pass.settings;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import mobile.android.pass.R;
import mobile.android.pass.utils.StorageHelper;

/**
 * Activity to show the full PublicKey
 */
public class PublicKeyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_public_key);

        String publicKey = new StorageHelper(this).getArmoredPublicKey();

        TextView publicKeyView = (TextView) findViewById(R.id.full_public_key_view);
        publicKeyView.setText(publicKey);

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
}
