package mobile.android.pass;

import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.spongycastle.openpgp.PGPUtil;

import java.security.Provider;
import java.security.Security;
import java.util.Iterator;

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
//            Log.d("MAIN", pgpHelper.getPublicKeyString());
        }

//        for (Provider provider : Security.getProviders()) {
//            System.out.format("\n----\nProvider: %s", provider.getName());
//
//            final Iterator<Object> i = provider.keySet().iterator();
//            while (i.hasNext()) {
//
//                String entry = (String) i.next();
//                System.out.format("\n%s \t %s",
//                        entry,
//                        provider.getProperty(entry)
//                );
//            }
//        }

        String response = "-----BEGIN PGP MESSAGE-----\n" +
                "Version: OpenPGP.js v2.2.0\n" +
                "Comment: http://openpgpjs.org\n" +
                "\n" +
                "wcBMA3eQesENh3D9AQgAmRNe6U7RCW5ZnfobimV2Llrpg7+xqTKtbu6hxDFV\n" +
                "G0oiXsgluIfSq+TPM5IOr9VSUUnvrsf6oOunVOoXSbSh81jFFYspwlBp152Y\n" +
                "HRkJo9AI8nnaRVQDzP/Tjg5f9065heulSN8M+RcJiKJkkfbz8fvDd9+Sckux\n" +
                "X4TQOqez7F8Y/qB9OMY7iaXj1maCLt7G8I6qf62TMsly5YCGAmP8e2VTQFX7\n" +
                "66yRISXQAc4DPs2Fk6f4DpzCIC5JA0TAcOsTKw4LpS3VX9RnBw06ckGOKRZA\n" +
                "RXEXDvS6orbXmnskCOG0fsMkEhmECsOVPn8Ewaugv+eJCNyKZqsqcYz5oJA6\n" +
                "JNI6Aa+VXl5YR8iIebgnkCghONR07aAl50maMarHcRY1X4wzhOXlEKMkMo/m\n" +
                "WydnPdYJFSLez2Zw99hsdg==\n" +
                "=JXQ6\n" +
                "-----END PGP MESSAGE-----\n";

        Log.d("MAIN", pgpHelper.decrypt(response, "TheKeyPassword"));


    }
}
