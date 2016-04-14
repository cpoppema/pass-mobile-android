package mobile.android.pass.secrets;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.TextView;
import android.widget.Toast;

import mobile.android.pass.utils.ClipboardHelper;

/**
 * Created by marcov on 14-4-16.
 */
public class SecretDialogHelper implements DialogInterface.OnClickListener {
    private Context mContext;
    private TextView mPasswordView;

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

        builder.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        ClipboardHelper.addToClipboard(mContext, mPasswordView.getText().toString());
        Toast.makeText(mContext, "Password copied to clipboard", Toast.LENGTH_SHORT).show();
    }
}
