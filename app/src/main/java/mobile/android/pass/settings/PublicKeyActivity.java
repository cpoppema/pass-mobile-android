package mobile.android.pass.settings;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Objects;

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
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
