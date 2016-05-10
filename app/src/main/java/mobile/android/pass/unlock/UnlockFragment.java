package mobile.android.pass.unlock;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import mobile.android.pass.R;
import mobile.android.pass.secrets.SecretsActivity;

public class UnlockFragment extends DialogFragment {
    private static final String TAG = UnlockFragment.class.toString();

    public UnlockFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setRetainInstance(true);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i(TAG, "onCreateDialog");
        // Do not call super, build our own dialog to set title and add button.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.fragment_unlock_title)
                // OnClickListener is set below, not here.
                .setPositiveButton(R.string.fragment_unlock_action_unlock, null);

        // Call default fragment methods to set View for Dialog from builder.
        View v = onCreateView(getActivity().getLayoutInflater(), null, null);
        onViewCreated(v, null);
        builder.setView(v);

        // Cannot put this in onCreateView.
        final EditText passphraseInput = (EditText) v.findViewById(R.id.unlock_passphrase);

        // Create dialog to return.
        final AlertDialog alertDialog = builder.create();

        // Bind OnClickListener to the button within the OnShowListener, otherwise the dialog
        // will ALWAYS be dismissed.
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String passphrase = passphraseInput.getText().toString();
                        Log.i(TAG, "Passphrase: " + passphrase);

                        // Validate passphrase.
                        boolean unlocked = false;
                        if (!passphrase.isEmpty()) {
                            // TODO: validate passphrase
                            unlocked = true;
                        }

                        Log.i(TAG, "Unlocked: " + Boolean.toString(unlocked));
                        if (unlocked) {
                            // Reset input.
                            passphraseInput.setError(null);
                            passphraseInput.getText().clear();

                            // Move on.
                            Log.i(TAG, "Start SecretsActivity");
                            startActivity(new Intent(getActivity(), SecretsActivity.class));
                        } else {
                            // Give feedback.
                            Log.i(TAG, "Show input feedback");
                            passphraseInput.setError(getString(R.string.fragment_unlock_invalid_passphrase));
                        }
                    }
                });
            }
        });

        // Let the activity know this fragment has been dismissed.
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                ((UnlockActivity) getActivity()).setDialogTag(UnlockActivity.NO_DIALOG_TAG);
            }
        });

        if (savedInstanceState == null) {
            // Show keyboard.
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

    @Override
    public void onDestroyView() {
        Log.i(TAG, "onDestroyView");
        // This is here because of a bug:
        // https://code.google.com/p/android/issues/detail?id=17423
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }

        super.onDestroyView();
    }
}
