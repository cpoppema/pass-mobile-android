package mobile.android.pass.secrets;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;

import org.apache.commons.codec.binary.Base32;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Contains all the data to show it in the list of secrets and to uniquely identify it when
 * retrieving the password for it.
 */
public class Secret implements Parcelable {
    public static final Parcelable.Creator<Secret> CREATOR = new Parcelable.Creator<Secret>() {
        @Override
        public Secret createFromParcel(Parcel in) {
            return new Secret(in);
        }

        @Override
        public Secret[] newArray(int size) {
            return new Secret[size];
        }
    };
    public static final String DOMAIN = "domain";
    public static final String PATH = "path";
    public static final String USERNAME = "username";
    public static final String USERNAME_NORMALIZED = "username_normalized";
    public static final String OTP = "otp";

    public static final String OTP_YES = "yes";
    public static final String OTP_NO = "no";

    private String mDomain;
    private String mPath;
    private String mUsername;
    private String mUsernameNormalized;
    private String mOtp;

    // These are used as instance variables only and are not parcelable.
    private String mSecretText;
    private String mPassphrase;

    public Secret(JSONObject object) {
        try {
            mDomain = object.getString(DOMAIN);
            mPath = object.getString(PATH);
            mUsername = object.getString(USERNAME);
            mUsernameNormalized = object.getString(USERNAME_NORMALIZED);
            mOtp = OTP_NO;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Secret(Cursor cursor) {
        mDomain = cursor.getString(cursor.getColumnIndex(DOMAIN));
        mPath = cursor.getString(cursor.getColumnIndex(PATH));
        mUsername = cursor.getString(cursor.getColumnIndex(USERNAME));
        mUsernameNormalized = cursor.getString(cursor.getColumnIndex(USERNAME_NORMALIZED));
        mOtp = cursor.getString(cursor.getColumnIndex(OTP));
    }

    private Secret(Parcel in) {
        String[] data = new String[5];

        in.readStringArray(data);
        mDomain = data[0];
        mPath = data[1];
        mUsername = data[2];
        mUsernameNormalized = data[3];
        mOtp = data[4];
    }

    // Factory method to convert an array of JSON objects into a list of objects
    // Secret.fromJson(jsonArray);
    public static ArrayList<Secret> fromJson(JSONArray jsonObjects) {
        ArrayList<Secret> secrets = new ArrayList<>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                secrets.add(new Secret(jsonObjects.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return secrets;
    }

    public String getDomain() {
        return mDomain;
    }

    public String getPath() {
        return mPath;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getUsernameNormalized() {
        return mUsernameNormalized;
    }

    public String getOtp() {
        return mOtp;
    }

    public void setOtpYes() {
        mOtp = OTP_YES;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{mDomain, mPath, mUsername, mUsernameNormalized, mOtp});
    }

    /**
     * Returns true if @needle was found in @hay using fuzzy matching.
     */
    private boolean fuzzyContains(String hay, String needle) {
        hay = hay.toLowerCase();
        needle = needle.toLowerCase();

        int lastIndex = -1;
        for (int i = 0; i < needle.length(); i++) {
            String l = Character.toString(needle.charAt(i));
            if ((lastIndex = hay.indexOf(l, lastIndex + 1)) == -1) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns true if @needle is empty or when it is found in either the domain, username
     * (normalized) using fuzzy matching.
     **/
    public boolean isMatch(String needle) {
        return TextUtils.isEmpty(needle) || fuzzyContains(getDomain(), needle) ||
                fuzzyContains(getUsername(), needle) || fuzzyContains(getUsernameNormalized(), needle);
    }

    public String getSecretText() {
        return mSecretText;
    }

    /**
     * Set the full contents of a secret, doing this allows for setting the passphrase "parsing"
     * in this class in a centralized place.
     */
    public void setSecretText(String secretText) {
        mSecretText = secretText;
    }

    public String getPassphrase() {
        if (mPassphrase == null) {
            // Read the first line as the password.
            mPassphrase = getSecretText().split("\n")[0];
        }
        return mPassphrase;
    }

    public String getToken() {
        String token = null;

        if (mSecretText != null && mSecretText.startsWith("otpauth://totp/")) {
            // TODO: Use an otp lib to extract otp secret from mSecretText to generate a token here ?
            Uri url = Uri.parse(mSecretText.split("\n")[0]);
            String encodedKey = url.getQueryParameter("secret");
            if (encodedKey != null && encodedKey.length() > 0) {
                Base32 base32 = new Base32();
                byte[] decodedSecret = base32.decode(encodedKey);
                SecretKey secretKey = new SecretKeySpec(decodedSecret, 0, decodedSecret.length, "AES");

                try {
                    TimeBasedOneTimePasswordGenerator totp = new TimeBasedOneTimePasswordGenerator();
                    token = String.valueOf(totp.generateOneTimePassword(secretKey, new Date()));
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
        }

        return token;
    }

    public Integer getTokenExpiresIn() {
        Calendar rightNow = Calendar.getInstance();
        int seconds = rightNow.get(Calendar.SECOND);
        return 30 - seconds % 30;
    }
}
