package mobile.android.pass;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;

import org.spongycastle.openpgp.PGPPrivateKey;

import mobile.android.pass.pgp.PgpHelper;

/**
 * Created by marco on 13/04/16.
 */
public class PasswordHelper implements DialogInterface.OnClickListener {
    private Context mContext;
    private EditText mPasswordInput;

    private PasswordCallback mCallback;
    private PgpHelper pgpHelper;

    public PasswordHelper(Context context, PasswordCallback passwordCallback) {
        mContext = context;
        mCallback = passwordCallback;
        pgpHelper = new PgpHelper(mContext);
    }

    public void askForPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Password");

        mPasswordInput = new EditText(mContext);
        mPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(mPasswordInput);

        builder.setPositiveButton("OK", this);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        String password = mPasswordInput.getText().toString();
        PGPPrivateKey privateKey = pgpHelper.getPrivateKey(password);

        // Clear password from memory.
        mPasswordInput.getText().clear();
        password = null;

        if (privateKey != null) {
            mCallback.onCorrectPassword(privateKey);
        } else {
            mCallback.onIncorrectPassword();
        }
    }
}
