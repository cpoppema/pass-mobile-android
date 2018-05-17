package mobile.android.pass.secrets;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import mobile.android.pass.R;
import mobile.android.pass.utils.ClipboardHelper;


public class TokenFragment extends DialogFragment {
    private static final String TAG = TokenFragment.class.toString();

    // Secret (object) shown in this fragment.
    private Secret mSecret;

    private TextView mTokenTextView;
    private TextView mTokenExpirationTextView;

    private Handler mHandler;
    private Runnable mTokenTimer;
    private int mTokenTimerInterval = 1000;
    private Boolean mValidToken = false;

    public TokenFragment() {
        // Required empty public constructor.
    }

    public static TokenFragment newInstance(Secret secret, String secretText) {
        TokenFragment f = new TokenFragment();

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

    private void showToken() {
        String token = mSecret.getToken();
        if (token != null) {
            mTokenTextView.setText(token);
            mTokenExpirationTextView.setText(getString(R.string.token_expiration_message, mSecret.getTokenExpiresIn()));
        } else {
            if (mSecret.getSecretText() != null && mSecret.getSecretText().startsWith("otpauth://hotp/")) {
                mTokenTextView.setText(R.string.token_hotp_error_message);
            } else {
                mTokenTextView.setText(R.string.token_generic_error_message);
            }
        }
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

        if (mSecret.getToken() != null) {
            mValidToken = true;
        }

        mHandler = new Handler();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i(TAG, "onCreateDialog");
        // Do not call super, build our own dialog to set title and add button.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(getTitle())
                .setNegativeButton(R.string.token_dialog_button_ok, null);

        // Call default fragment methods to set View for Dialog from builder.
        View v = onCreateView(getActivity().getLayoutInflater(), null, null);
        onViewCreated(v, null);
        builder.setView(v);

        // Cannot put this in onCreateView nor onViewCreated.
        mTokenTextView = (TextView) v.findViewById(R.id.token_text);
        mTokenExpirationTextView = (TextView) v.findViewById(R.id.token_expiration_text);

        if (mValidToken) {
            builder.setPositiveButton(R.string.token_dialog_button_copy, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ClipboardHelper.copy(getContext().getApplicationContext(), mSecret.getToken());
                    Toast.makeText(getContext().getApplicationContext(),
                            getString(R.string.toast_copy_secret_token), Toast.LENGTH_SHORT)
                            .show();
                }
            });

            // Refresh token every second.
            mTokenTimer = new Runnable() {
                @Override
                public void run() {
                    AlertDialog dialog = (AlertDialog) getDialog();
                    if (mSecret.getTokenExpiresIn() < 3) {
                        Button copyButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        if (copyButton != null) {
                            copyButton.setEnabled(false);
                        }
                    } else {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    }

                    try {
                        showToken();
                    } finally {
                        mHandler.postDelayed(mTokenTimer, mTokenTimerInterval);
                    }
                }
            };
        } else {
            showToken();
        }

        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_token, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");

        // onResume is also called after onCreate.
        startTokenTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");

        // onPause is also called before onDestroy.
        stopTokenTimer();
    }

    void startTokenTimer() {
        if (mTokenTimer != null) {
            mTokenTimer.run();
        }
    }

    void stopTokenTimer() {
        if (mTokenTimer != null) {
            mHandler.removeCallbacks(mTokenTimer);
        }
    }
}