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

//        String response = "-----BEGIN PGP MESSAGE-----\n" +
//                "\n" +
//                "hQEMA4yup+YSXRpVAQgAkJ3TgAcrtnq3dHoaJXiL9JFn7CHhe75BRwv6cd6l\n" +
//                "twoZkwHJGNXLlx6cqr50cN+UwMJ8qtG+5NYW2R8wveBp8kZoUodcdRcwjxkt\n" +
//                "EiLLpK0HmJ/fYdu20CwHtyD7iP0uxQm3O9fAsF6ds64+KjjuiCHDi1nu5d6K\n" +
//                "S4Kme3csmWXvDPokA65JEcnl/wi6Se9yOT/ntWOU2Mk18vXQ7mUmSiKOXvcN\n" +
//                "THxzl1rn7G37XjjnfCZHU1LEAqjq6h125baxELKo8s+x20gxJBGN8Cqt5Qy6\n" +
//                "PBYfb3tWo0lKhyRZ/4l67T1sE+6zjC3fZWgppZvAHtwBdpFRR+RPU3jXkLzj\n" +
//                "moUBDAN3kHrBDYdw/QEH/RzhW4Dn606HUeUAFW1F4CS0LCZJJQ2XNBgUGGPQ\n" +
//                "ChEJkf436okdXaLW8LZ58WT8gQcDSaRSG/WzU158oCX3VZKr5jP75nyGQXhM\n" +
//                "PnF1sj/rB2jIfrvsbu5XOq7Z6ZIHn9FjxToul/13KqQWBnPmc+so2My/Qs9K\n" +
//                "M4YI/oxv/HdIKsFE05+IXjeAZm4ef4SQa3Pimg+PEVJodFqJKirh7c1FB4WZ\n" +
//                "q5tcY659zUIGDLqa6BAYClAQ17Q+JtoK+5/JsKPp9C115cZOZIFV3Kruq8VS\n" +
//                "Z+0E3nbL7F7OA8g8h41qU5lbEx507xiv6zxDZ23hy46WP5osC02xCtF46qow\n" +
//                "YdjJHvhu6guZhPBG5QPWK2m0nRAKBSKFX1sBXy217S2LxA==\n" +
//                "=RiYf\n" +
//                "-----END PGP MESSAGE-----";

        String response = "-----BEGIN PGP MESSAGE-----\n" +
                "Version: OpenPGP.js v2.2.0\n" +
                "Comment: http://openpgpjs.org\n" +
                "\n" +
                "wcBMA3eQesENh3D9AQf/Sp8+xUMHZjTwdj6wnwryj1Kg1mWEZOezMxB29tz1\n" +
                "ZuM9NEP/M9hGmp0eSlqe9eChY4GNYzJhas5PK1QYPnVTwzzrjJvawix+3XQb\n" +
                "W2UBPLdpgxGIvuEyFaeL12yMO8E5zsKRgSe7sZetaq+vbDn8JSBAsdASX4QF\n" +
                "ngp9r9QrgXTujV64hQcis+wyaymKwpq57aa1sVaRary/iFyvVXplYtVIw9yi\n" +
                "ys09YucId3S4H49L4LMbcwCRAGthpqM2Diwj0JBtYl2l1u2PyKSAexoJepjz\n" +
                "8IiTl72rQklqKjIpn6+yorxs3ZdYpzVeePXDajj+9DZS/ujX3ASyBTDQmCpP\n" +
                "cdLA6wF54bAu/DanjQV0uzvabEOsXlrDI5xVFUctnXGfK7KjYU+AmkNGSNvR\n" +
                "ZYhMZ2Ceq/8NncgY22jtXdYJ7CT9nPfOZWeP2cBHWkHvKZuOJt43ATQZIzM8\n" +
                "p6txNcIMmpjV7YmlS7PqjRPN8T5wDQIYWmqBxi1KogH+t4sW2KnGaC/ZSyxQ\n" +
                "JKPpad7pkQLy7n8Wgp53e0IZNQahCfrldvj/kZX3Niv0Wem1p4a+T6dg3N8v\n" +
                "rvlJBDUiVGkrFG3y9dMYIFVrxGhBjmO505wgvqhGDgMU+VQO5YUvRYq8A2WM\n" +
                "6YcXB8wImuZQ7T3rfcLw5YtMd0/poAeIuHk2NVFWEYcoP8ynuXe86EHPPsLp\n" +
                "87BJPIFe3r/SaarV5oexoN4H2FHh3WKoCDLpAAqIFnhOcCxL21ahTHX2yuvk\n" +
                "XFcMEOE1p8wAJ7mJOcYP8+CUXVZ9DR6Z111+Q/q5Y+Gla7hwVYlyn1Rc+gXG\n" +
                "9KXfbKdfYaBCYwZ4xFhI/NgFzeidyAH4yDYjFnPCeOpa2/3aARTSWzx3xndt\n" +
                "LT2V/8u4l72zxWlRBzTU39I/HEb9ZN7X9Ng=\n" +
                "=JTvB\n" +
                "-----END PGP MESSAGE-----";

        Log.d("MAIN", pgpHelper.decrypt(response, "TheKeyPassword"));


    }
}
