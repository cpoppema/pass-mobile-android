package mobile.android.pass.secrets;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import mobile.android.pass.R;
import mobile.android.pass.utils.ClipboardHelper;


public class SecretFragment extends DialogFragment {
    private static final String TAG = SecretFragment.class.toString();

    // Secret (object) shown in this fragment.
    private Secret mSecret;

    public SecretFragment() {
        // Required empty public constructor.
    }

    public static SecretFragment newInstance(Secret secret, String secretText) {
        SecretFragment f = new SecretFragment();

        // Supply secret as an argument.
        Bundle args = new Bundle();
        args.putParcelable("secret", secret);
        args.putString("secretText", secretText);
        f.setArguments(args);

        return f;
    }

    private String getTitle() {
        return mSecret.getDomain();
    }

    private String getMessage() {
        return mSecret.getUsername() + "\n" + mSecret.getSecretText();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        // All views (in this case just one) we want to retain on a config change (e.g. rotation),
        // is handled automatically because it has an id. No need to call setRetainInstance(true).

        // Get secret from bundle.
        mSecret = getArguments().getParcelable("secret");
        String secretText = getArguments().getString("secretText");
        mSecret.setSecretText(secretText);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i(TAG, "onCreateDialog");
        // Do not call super, build our own dialog to set title and add button.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(getTitle())
                .setNegativeButton(R.string.secret_dialog_button_ok, null)
                .setPositiveButton(R.string.secret_dialog_button_copy, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ClipboardHelper.copy(getContext().getApplicationContext(), mSecret.getPassphrase());
                            Toast.makeText(getContext().getApplicationContext(),
                                    getString(R.string.toast_copy_secret_password), Toast.LENGTH_SHORT)
                                    .show();
                        }
                });


        // Call default fragment methods to set View for Dialog from builder.
        View v = onCreateView(getActivity().getLayoutInflater(), null, null);
        onViewCreated(v, null);
        builder.setView(v);

        // Cannot put this in onCreateView nor onViewCreated.
        TextView secretTextView = (TextView) v.findViewById(R.id.secret_text);
        secretTextView.setText(getMessage());

        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_secret, container, false);
    }
}