package mobile.android.pass.secrets;


import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import mobile.android.pass.R;
import mobile.android.pass.utils.ClipboardHelper;

public class SecretDialogHelper implements DialogInterface.OnClickListener {
    private Context mContext;
    private AlertDialog mAlertDialog;

    private String mPassword;

    public SecretDialogHelper(Context context) {
        mContext = context;
    }

    public void showSecretDialog(Secret secret, String password) {
        mPassword = password;

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(secret.getDomain());
        builder.setMessage(secret.getUsername() + "\n" + mPassword);

        builder.setPositiveButton(mContext.getString(R.string.dialog_button_copy), this);
        builder.setNegativeButton(mContext.getString(R.string.dialog_button_ok),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPassword = null;
                dialog.cancel();
            }
        });

        mAlertDialog = builder.show();
    }

    public boolean isShowing() {
        if (mAlertDialog != null) {
            return mAlertDialog.isShowing();
        }
        return false;
    }

    /**
     * Function to close an already active secret dialog.
     */
    public void closeSecretDialog() {
        if (mAlertDialog != null) {
            mPassword = null;
            mAlertDialog.cancel();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        Context appContext = mContext.getApplicationContext();
        ClipboardHelper.copy(appContext, mPassword);
        Toast.makeText(appContext,
                appContext.getString(R.string.toast_copy_secret_password), Toast.LENGTH_SHORT)
                .show();
    }
}