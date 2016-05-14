//package mobile.android.pass.utils;
//
//import android.content.Context;
//import android.content.DialogInterface;
//import android.support.v7.app.AlertDialog;
//import android.text.InputType;
//import android.widget.EditText;
//
//import org.spongycastle.openpgp.PGPPrivateKey;
//
///**
// * Class used for the password dialog process.
// */
//public class PasswordHelper implements DialogInterface.OnClickListener {
//    private AlertDialog mAlertDialog;
//    private Context mContext;
//    private EditText mPasswordInput;
//
//    private PasswordCallback mCallback;
//    private PgpHelper pgpHelper;
//
//    /**
//     * Constructor
//     * @param context
//     * @param passwordCallback Class that holds the functions to perform callbacks on.
//     */
//    public PasswordHelper(Context context, PasswordCallback passwordCallback) {
//        mContext = context;
//        mCallback = passwordCallback;
//        pgpHelper = new PgpHelper(mContext);
//    }
//
//    /**
//     * Function to create a dialog that prompts for the password. This function does not invoke
//     * show but only creates a AlertDialog object.
//     * @param title Title of the dialog.
//     */
//    private void createPasswordDialog(String title) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//        builder.setTitle(title);
//
//        mPasswordInput = new EditText(mContext);
//        mPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
//        builder.setView(mPasswordInput);
//
//        builder.setPositiveButton("OK", this);
//        builder.setNegativeButton("DISMISS", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//            }
//        });
//
//        mAlertDialog = builder.create();
//    }
//
//    /**
//     * Function that shows the password dialog with title `password`.
//     */
//    public void askForPassword() {
//        askForPassword("Password");
//    }
//
//    /**
//     * Function that shows the password dialog with the given title.
//     * @param title
//     */
//    public void askForPassword(String title) {
//        if (mAlertDialog == null || !mAlertDialog.isShowing()) {
//            createPasswordDialog(title);
//            mAlertDialog.show();
//        }
//    }
//
//    @Override
//    public void onClick(DialogInterface dialog, int which) {
//        String password = mPasswordInput.getText().toString();
//        PGPPrivateKey privateKey = pgpHelper.getArmoredPrivateKey(password);
//
//        // Clear password from memory.
//        mPasswordInput.getText().clear();
//        password = null;
//
//        mAlertDialog.cancel();
//
//        // When null that password was incorrect.
//        if (privateKey != null) {
//            mCallback.onCorrectPassword(privateKey);
//        } else {
//            mCallback.onIncorrectPassword();
//        }
//    }
//}
