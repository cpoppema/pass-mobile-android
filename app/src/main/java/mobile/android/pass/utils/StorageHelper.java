package mobile.android.pass.utils;

import org.spongycastle.openpgp.PGPSecretKey;

import android.content.Context;
import android.content.SharedPreferences;

import java.nio.charset.StandardCharsets;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

/**
 * Class that acts as middleware between a storage engine and the app.
 */
public class StorageHelper {
    private final SharedPreferences mSharedPreferences;

    /**
     * Constructor.
     */
    public StorageHelper(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private String getString(StorageKey key) {
        return mSharedPreferences.getString(key.toString(), "");
    }

    public void putString(StorageKey key, String value) {
        mSharedPreferences.edit().putString(key.toString(), value).apply();
    }

    public String getKeyName() {
        return this.getString(StorageKey.PUBLIC_KEY_NAME);
    }

    public void putKeyName(String keyName) {
        this.putString(StorageKey.PUBLIC_KEY_NAME, keyName);
    }

    public void putKeyPair(PGPSecretKey keyPair) {
        String publicKey = PgpHelper.extractArmoredPublicKey(keyPair);
        String privateKey = PgpHelper.extractArmoredPrivateKey(keyPair);
        String keyID = PgpHelper.getKeyID(keyPair);

        this.putArmoredPublicKey(publicKey);
        this.putArmoredPrivateKey(privateKey);
        this.putKeyID(keyID);
    }

    /** Key for debug/review purposes **/
    public void putDebugKeyPair() {
        String debugPublicKey = "-----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                "Version: BCPG v@RELEASE_NAME@\n" +
                "\n" +
                "mQENBGYFwmgBCADSA/s3QR7tMyLN+yt1VejPp4NZ5GYxI//Hz4wJVlC3DRxp0fw5\n" +
                "zSpi6j3d3jEBEEoVo/v/+3oF//ajJpnfkrJdSK80Xo0Vwh7/qcBjhvJzzn9ztJbb\n" +
                "xAnxmh6i96qF9pMwVSEDPhL5fQsYz+w+Yu4omdNnGXrT+1bne5P76A6CpclXRJua\n" +
                "R4PCVku2/bTOlHsQnIb4yttIP0GEo7EKR6+aUEKvTM7W2yRsonDSyUjIY74yWHhG\n" +
                "LALnr+n5Km2IX7cKUESaYAGE46YtaaAjxLd+3m21+O0/7is1aIaVEu3a4rZgeyCk\n" +
                "7o2UzPIQV410++Si9CG5F7hKvcafwHrmVWgVABEBAAG0EGFuZHJvaWQgZGVtbyBr\n" +
                "ZXmJARwEEAECAAYFAmYFwmgACgkQCdrBIheUCn7f+Qf/aDA68YfB88onBxk5Lfnt\n" +
                "LxrPrrqtnQCZGthU0oLSV7ypB/M8No9lIMm5fmzeHsqo0FiywNDx/1XlL6lgrtRE\n" +
                "d3+vpDM01s5x2GmgRPrODmm7Ex5dIEPZqzx+YpsG+XeNDcs0Bcyn9hmhrhDnWQXU\n" +
                "AoYh43YeMMqrdAIYdqdXKK67z1/eQ3ZwiCdyhL0amrUwSOVfWdnyeZP1ZMJlWp3O\n" +
                "fBFRXHuyhF9G7EiTAIXE6sc6egRPCJZoNAMu5Xdbk2e+WDwiH4AOed4J1nSs7Elo\n" +
                "cLjP4L4ddKam+nAuf7dP3z3jZViJWDL/iGX1MEKpmdsOuzhY2/K1vAg6uFy6tmUC\n" +
                "kw==\n" +
                "=K/K8\n" +
                "-----END PGP PUBLIC KEY BLOCK-----";
        String debugPrivateKey = "-----BEGIN PGP PRIVATE KEY BLOCK-----\n" +
                "Version: BCPG v@RELEASE_NAME@\n" +
                "\n" +
                "lQO+BGYFwmgBCADSA/s3QR7tMyLN+yt1VejPp4NZ5GYxI//Hz4wJVlC3DRxp0fw5\n" +
                "zSpi6j3d3jEBEEoVo/v/+3oF//ajJpnfkrJdSK80Xo0Vwh7/qcBjhvJzzn9ztJbb\n" +
                "xAnxmh6i96qF9pMwVSEDPhL5fQsYz+w+Yu4omdNnGXrT+1bne5P76A6CpclXRJua\n" +
                "R4PCVku2/bTOlHsQnIb4yttIP0GEo7EKR6+aUEKvTM7W2yRsonDSyUjIY74yWHhG\n" +
                "LALnr+n5Km2IX7cKUESaYAGE46YtaaAjxLd+3m21+O0/7is1aIaVEu3a4rZgeyCk\n" +
                "7o2UzPIQV410++Si9CG5F7hKvcafwHrmVWgVABEBAAH+AwMCcjcqtNY+b5RgjHH9\n" +
                "tuU1cQEvEWIOjCi5UKIkzKDn9CqppzwQoY5t/etOd7WuZ8YpnE3jSM6NL+RACbix\n" +
                "+LX8AasPGyqtNZYqQvn+76kxwstMz0NSjOOiBtDENtAqZ0A+CpDvE4yKen+vy++p\n" +
                "kJfzkBd1kaQ96r+BUGg6ybjMoHwubLnvHfql9QggaT7qXuR/hF/pkwXsSnW4rVE8\n" +
                "rtTZBkcQVEWBaf+dN72tceSCuxomfFxAX3B1IKsTI+/Vty+TAtTCbmUDaPIWnsE7\n" +
                "E6pGYYYyNsU+uruLcggbpxNxP2h+bztmn6fYqYBUfAmkkDRPf8JliEVyYM5lRZaC\n" +
                "9cW0P0+KCBil5uTKxceqdXV0kFFsTYeYTpX9xImb39wT+Mm3hN+SQVkk/HJlXnaP\n" +
                "QsjlquQiakFtwqAnbycbXWLsjKsLVjxHeA0569VTvODbhFnDe1LRV5qi9X4QaFxq\n" +
                "e0zgavWkhskIpD+RSbJqFFtuMw/Q5UWpe8F5HSWVdkF1uBT8C5YxDi+1ucXQHHnt\n" +
                "s2WYIOOkbopNm/A2KYfwzBDvZrchP8agQiFFtVZFpTId/FLw8MFPlplZzTt9Ug8W\n" +
                "KLviy2Z/rIO5hF2VXk2DoS/sVyDhwFkR6r7TghhrFrPzuT+vxALmj+VPQisr9655\n" +
                "266qvV8l9NyCK7Dw+dAKn+/J/5oYJcfPZbXvoJCfMz0qw7DCeUmgwNaxzpG7qXWP\n" +
                "5YBz5QFNhQHEKypCpdlGrJ4FRKEsnLYB5tGfz+f0HfkxB3An/hh5GlsfkRTOZfgz\n" +
                "n7H13IZYdCkuPNlclkggbDiE5zLOS1azGRhn/VyFTFyTr33ntc8NCOu2sIu5Rrxq\n" +
                "pejYdQ/PCGtn01SpTF9IWNOGYtIHku56p7Kf2JWje21+D8ReajLqfJuq7npL7f1K\n" +
                "+rQQYW5kcm9pZCBkZW1vIGtleYkBHAQQAQIABgUCZgXCaAAKCRAJ2sEiF5QKft/5\n" +
                "B/9oMDrxh8HzyicHGTkt+e0vGs+uuq2dAJka2FTSgtJXvKkH8zw2j2Ugybl+bN4e\n" +
                "yqjQWLLA0PH/VeUvqWCu1ER3f6+kMzTWznHYaaBE+s4OabsTHl0gQ9mrPH5imwb5\n" +
                "d40NyzQFzKf2GaGuEOdZBdQChiHjdh4wyqt0Ahh2p1corrvPX95DdnCIJ3KEvRqa\n" +
                "tTBI5V9Z2fJ5k/VkwmVanc58EVFce7KEX0bsSJMAhcTqxzp6BE8Ilmg0Ay7ld1uT\n" +
                "Z75YPCIfgA553gnWdKzsSWhwuM/gvh10pqb6cC5/t0/fPeNlWIlYMv+IZfUwQqmZ\n" +
                "2w67OFjb8rW8CDq4XLq2ZQKT\n" +
                "=PWmc\n" +
                "-----END PGP PRIVATE KEY BLOCK-----";
        String debugKeyID = "9DAC12217940A7E";
        String debugKeyName = "android demo key";

        this.putArmoredPublicKey(debugPublicKey);
        this.putArmoredPrivateKey(debugPrivateKey);
        this.putKeyID(debugKeyID);
        this.putKeyName(debugKeyName);
    }

    public void putArmoredPublicKey(String publicKey) {
        this.putString(StorageKey.PUBLIC_KEY, publicKey);
    }

    public String getArmoredPublicKey() {
        return this.getString(StorageKey.PUBLIC_KEY);
    }

    public void putArmoredPrivateKey(String privateKey) {
        this.putString(StorageKey.PRIVATE_KEY, privateKey);
    }

    public String getArmoredPrivateKey() {
        return this.getString(StorageKey.PRIVATE_KEY);
    }

    public byte[] getPrivateKey() {
        return this.getArmoredPrivateKey().getBytes(StandardCharsets.UTF_8);
    }

    public void putKeyID(String keyID) {
        this.putString(StorageKey.PUBLIC_KEY_ID, keyID);
    }

    public String getKeyID() {
        return this.getString(StorageKey.PUBLIC_KEY_ID);
    }

    public String getServerAddress() {
        return this.getString(StorageKey.SERVER_ADDRESS);
    }

    public void putServerAddress(String serverAddress) {
        this.putString(StorageKey.SERVER_ADDRESS, serverAddress);
    }

    public enum StorageKey {
        PRIVATE_KEY("prey_key_private_key"),
        PUBLIC_KEY("prey_key_public_key"),
        PUBLIC_KEY_NAME("pref_key_key_name"),
        PUBLIC_KEY_ID("pref_key_public_key_id"),
        SERVER_ADDRESS("pref_key_server");

        private final String key;

        StorageKey(String key) {
            this.key = key;
        }

        @NonNull
        @Override
        public String toString() {
            return this.key;
        }
    }
}
