package mobile.android.pass;

import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PgpHelper pgpHelper = new PgpHelper(this);

        if (!PreferenceManager.getDefaultSharedPreferences(this).contains("public")) {
            pgpHelper.generateKeyPair("TheKeyName", "TheKeyPassword");
            Log.d("MAIN", pgpHelper.getPublicKeyString());
        } else {
            Log.d("MAIN", "KEY EXISTS");
        }
    }
}
