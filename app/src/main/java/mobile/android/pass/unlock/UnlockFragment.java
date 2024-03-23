package mobile.android.pass.unlock;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import mobile.android.pass.R;
import mobile.android.pass.secrets.SecretsActivity;
import mobile.android.pass.utils.PgpHelper;
import mobile.android.pass.utils.StorageHelper;

/** Shows a dialog that can unlock the local keypair when the correct passphrase is entered. **/

public class UnlockFragment extends DialogFragment {
    private static final String TAG = UnlockFragment.class.toString();

    // Storage reference.
    private StorageHelper mStorageHelper;

    public UnlockFragment() {
        // Required empty public constructor.
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        // All views (in this case just one) we want to retain on a config change (e.g. rotation),
        // is handled automatically because it has an id. No need to call setRetainInstance(true).

        // Instantiate custom storage interface.
        mStorageHelper = new StorageHelper(getActivity());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i(TAG, "onCreateDialog");
        // Do not call super, build our own dialog to set title and add button.
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.fragment_unlock_title)
                // OnClickListener is set below, not here.
                .setPositiveButton(R.string.fragment_unlock_action_unlock, null);

        // Call default fragment methods to set View for Dialog from builder.
        View v = onCreateView(requireActivity().getLayoutInflater(), null, null);
        assert v != null;
        onViewCreated(v, null);
        builder.setView(v);

        // Cannot put this in onCreateView nor onViewCreated.
        final EditText passphraseInput = (EditText) v.findViewById(R.id.unlock_passphrase);

        // Create dialog to return.
        final AlertDialog alertDialog = builder.create();

        // Bind OnClickListener to the button within the OnShowListener, otherwise the dialog
        // will ALWAYS be dismissed.
        alertDialog.setOnShowListener(dialog -> {
            Button button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            button.setOnClickListener(v1 -> {
                String passphrase = passphraseInput.getText().toString();
                Log.i(TAG, "Passphrase: " + passphrase);

                // Validate passphrase.
                boolean unlocked = false;
                if (!passphrase.isEmpty()) {
                    unlocked = PgpHelper.testPassphraseForKey(mStorageHelper.getPrivateKey(), passphrase);
                }

                Log.i(TAG, "Unlocked: " + unlocked);
                if (unlocked) {
                    // Reset input.
                    passphraseInput.setError(null);
                    passphraseInput.getText().clear();

                    // Move on.
                    Log.i(TAG, "Start SecretsActivity");
                    startActivity(new Intent(getActivity(), SecretsActivity.class).putExtra("mPassphrase", passphrase));
                } else {
                    // Give feedback.
                    Log.i(TAG, "Show input feedback");
                    passphraseInput.setError(getString(R.string.fragment_unlock_invalid_passphrase));
                }
            });
        });

        // Let the activity know this fragment has been dismissed.
        alertDialog.setOnDismissListener(dialogInterface -> ((UnlockActivity) requireActivity()).setDialogTag(UnlockActivity.NO_DIALOG_TAG));

        if (savedInstanceState == null) {
            // Show keyboard.
            passphraseInput.requestFocus();
            alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        return alertDialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_unlock, container, false);
    }
}
