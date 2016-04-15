package mobile.android.pass.secrets;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.TextView;
import android.widget.Toast;

import mobile.android.pass.utils.ClipboardHelper;

/**
 * Helper for showing the dialog that contains the password.
 */
public class SecretDialogHelper implements DialogInterface.OnClickListener {
    private Context mContext;
    private AlertDialog mAlertDialog;

    public SecretDialogHelper(Context context) {
        mContext = context;
    }

    public void showSecretDialog(Secret secret, String password) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(secret.getDomain());
        builder.setMessage(password);

        builder.setPositiveButton("COPY", this);
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        mAlertDialog = builder.show();
    }

    /**
     * Function to close an already active secret dialog.
     */
    public void closeSecretDialog() {
        if (mAlertDialog != null) {
            mAlertDialog.cancel();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        ClipboardHelper.addToClipboard(mContext,
                ((TextView) mAlertDialog.findViewById(android.R.id.message)).getText().toString());
        Toast.makeText(mContext, "Password copied to clipboard", Toast.LENGTH_SHORT).show();
    }
}
