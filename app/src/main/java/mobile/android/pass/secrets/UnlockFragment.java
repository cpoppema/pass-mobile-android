package mobile.android.pass.secrets;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import mobile.android.pass.R;

public class UnlockFragment extends DialogFragment {

    private EditText mPassphraseInput;
    private AlertDialog mDialog;

    public UnlockFragment() {
        // Required empty public constructor
    }

    // Your own onCreate_Dialog_View method
    public View onCreateDialogView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_unlock, container); // inflate here
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.secrets_dialog_title_unlock)
                .setPositiveButton(R.string.unlock_button, null);

        // Call default fragment methods and set view for dialog
        View view = onCreateDialogView(getActivity().getLayoutInflater(), null, null);
        mPassphraseInput = (EditText) view.findViewById(R.id.unlock_passphrase);
        onViewCreated(view, null);
        dialogBuilder.setView(view);

        // Register the a OnClickListener after the dialog is visible, otherwise the dialog will
        // be dismissed, always.
        mDialog = dialogBuilder.create();
        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button button = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Object privateKey = null;

                        // Test passphrase.
                        String passphrase = mPassphraseInput.getText().toString();
                        if (!passphrase.isEmpty()) {
                            // TODO: test passphrase
                            Log.i("pass", "testing passphrase");
                            privateKey = true;
                        }

                        // When null the passphrase was invalid.
                        if (privateKey == null) {
                            // Give feedback.
                            mPassphraseInput.setError(getString(R.string.secrets_invalid_passphrase));
                        } else {
                            // Reset error.
                            mPassphraseInput.setError(null);

                            // Clear password from memory.
                            mPassphraseInput.getText().clear();

                            // Move on.
                            dialog.cancel();
                            startActivity(new Intent(getActivity(), SecretsActivity.class));
                        }
                    }
                });
            }
        });

        // Show soft keyboard automatically and request focus to field
        mPassphraseInput.requestFocus();
        mDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return mDialog;
    }
}
