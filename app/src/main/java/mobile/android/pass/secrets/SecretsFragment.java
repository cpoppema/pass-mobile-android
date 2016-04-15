package mobile.android.pass.secrets;

import android.app.ListFragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.spongycastle.openpgp.PGPPrivateKey;

import java.util.List;

import mobile.android.pass.utils.PasswordCallback;
import mobile.android.pass.utils.PasswordHelper;
import mobile.android.pass.R;
import mobile.android.pass.api.Api;
import mobile.android.pass.api.ApiService;
import mobile.android.pass.api.BaseParams;
import mobile.android.pass.api.SecretParams;
import mobile.android.pass.api.SecretResponse;
import mobile.android.pass.api.SecretsResponse;
import mobile.android.pass.utils.Storage;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment to show the secrets list and handle the password dialog that belongs to a secret.
 */
public class SecretsFragment
        extends ListFragment
        implements SwipeRefreshLayout.OnRefreshListener,
        Callback,
        PasswordCallback {
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private Api mApi;
    private PasswordHelper mPasswordHelper;
    private PGPPrivateKey mPrivateKey;
    private SecretDialogHelper mSecretDialogHelper;
    private Storage mStorage;

    private List<Secret> mSecrets;
    private Secret mCurrentSecret;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPasswordHelper = new PasswordHelper(getActivity(), this);
        mApi = ApiService.createApiService(getActivity());
        mSecretDialogHelper = new SecretDialogHelper(getActivity());
        mStorage = new Storage(getActivity());
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_secrets, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPasswordHelper.askForPassword();
    }

    @Override
    public void onStop() {
        super.onStop();

        cleanUp();
    }

    /**
     * Function to clean all memory references to password and private key.
     */
    private void cleanUp() {
        setListAdapter(null);
        mPrivateKey = null;
        mSecrets = null;
        mCurrentSecret = null;
        mSecretDialogHelper.closeSecretDialog();
    }

    @Override
    public void onRefresh() {
        if (mPrivateKey != null) {
            loadSecrets();
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
            mPasswordHelper.askForPassword();
        }
    }

    /**
     * Function to fetch secrets from server.
     */
    private void loadSecrets() {
        BaseParams params = new BaseParams(mStorage.getPublicKey());
        Call<SecretsResponse> call = mApi.secrets(params);
        call.enqueue(this);
    }

    /**
     * Function to load the password for a secret.
     * @param secret
     */
    private void loadSecret(Secret secret) {
        mCurrentSecret = secret;
        SecretParams params = new SecretParams(mStorage.getPublicKey(),
                secret.getUsername(), secret.getPath());
        Call<SecretResponse> call = mApi.secret(params);
        call.enqueue(this);
    }

    @Override
    public void onCorrectPassword(PGPPrivateKey privateKey) {
        mPrivateKey = privateKey;
        loadSecrets();
    }

    @Override
    public void onIncorrectPassword() {
        mPasswordHelper.askForPassword("Incorrect password");
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        loadSecret(mSecrets.get(position));
    }

    @Override
    public void onResponse(Call call, Response response) {
        if (response.isSuccess()) {

            Object object = response.body();

            if (object instanceof SecretsResponse) {
                mSecrets = ((SecretsResponse) object).getSecrets(getActivity(), mPrivateKey);
                SecretsAdapter secretsAdapter = new SecretsAdapter(getActivity(),
                        android.R.layout.simple_list_item_2, mSecrets);
                setListAdapter(secretsAdapter);
            } else if (object instanceof SecretResponse) {
                String password = ((SecretResponse) object).decryptResponseData(
                        getActivity(), mPrivateKey);
                mSecretDialogHelper.showSecretDialog(mCurrentSecret, password);
                mCurrentSecret = null;
            }

        }
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onFailure(Call call, Throwable t) {
        mSwipeRefreshLayout.setRefreshing(false);
    }
}
