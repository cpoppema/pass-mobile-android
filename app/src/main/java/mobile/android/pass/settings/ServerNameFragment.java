package mobile.android.pass.settings;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import mobile.android.pass.R;
import mobile.android.pass.utils.StorageHelper;

/** Shows a dialog to edit the server address using for all api requests. **/

public class ServerNameFragment extends DialogFragment {
    private static final String TAG = ServerNameFragment.class.toString();

    // Storage reference.
    private StorageHelper mStorageHelper;

    public ServerNameFragment() {
        // Required empty public constructor.
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        // All views (in this case just one) we want to retain on a config change (e.g. rotation),
        // is handled automatically because it has an id. No need to call setRetainInstance(true).

        mStorageHelper = new StorageHelper(getActivity());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i(TAG, "onCreateDialog");
        // Do not call super, build our own dialog to set title and add button.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.fragment_server_name_title)
                // OnClickListener is set below, not here.
                .setPositiveButton(R.string.fragment_server_name_button_ok, null)
                .setNegativeButton(R.string.fragment_server_name_button_cancel, null);

        // Call default fragment methods to set View for Dialog from builder.
        View v = onCreateView(getActivity().getLayoutInflater(), null, null);
        onViewCreated(v, null);
        builder.setView(v);

        // Cannot put this in onCreateView nor onViewCreated.
        final EditText serverInput = (EditText) v.findViewById(R.id.server_name);
        serverInput.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        if (!TextUtils.isEmpty(mStorageHelper.getServerAddress())) {
            serverInput.setText(mStorageHelper.getServerAddress());
        } else {
            serverInput.setText("https://");
        }

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
                        String serverAddress = serverInput.getText().toString();

                        // Validate server address.
                        boolean valid = false;
                        if (!serverAddress.isEmpty()) {
                            valid = (serverAddress.startsWith("http://") && serverAddress.length() > 7 ||
                                    serverAddress.startsWith("https://") && serverAddress.length() > 8);
                        }

                        if (valid) {
                            // Reset input.
                            serverInput.setError(null);
                            serverInput.getText().clear();

                            // Save and dismiss.
                            mStorageHelper.putServerAddress(serverAddress);
                            alertDialog.dismiss();
                        } else {
                            // Give feedback.
                            Log.i(TAG, "Show input feedback");
                            serverInput.setError(getString(R.string.fragment_server_name_invalid));
                        }
                    }
                });
            }
        });

        // Let the activity know this fragment has been dismissed.
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                ((SettingsActivity) getActivity()).setDialogTag(SettingsActivity.NO_DIALOG_TAG);
            }
        });

        if (savedInstanceState == null) {
            // Show keyboard.
            serverInput.requestFocus();
            alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        return alertDialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_server_name, container, false);
    }
}
