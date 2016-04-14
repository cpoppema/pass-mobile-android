package mobile.android.pass;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

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

        mPasswordView = new TextView(mContext);
        mPasswordView.setText(password);
        builder.setView(mPasswordView);

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
