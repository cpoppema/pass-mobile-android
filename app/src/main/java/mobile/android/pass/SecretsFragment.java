package mobile.android.pass;

import android.app.ListFragment;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.spongycastle.openpgp.PGPPrivateKey;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import mobile.android.pass.api.Api;
import mobile.android.pass.api.ApiResponse;
import mobile.android.pass.api.ApiService;
import mobile.android.pass.api.BaseParams;
import mobile.android.pass.pgp.PgpHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by marcov on 13-4-16.
 */
public class SecretsFragment
        extends ListFragment
        implements SwipeRefreshLayout.OnRefreshListener,
        Callback<ApiResponse>,
        PasswordCallback {
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private PasswordHelper mPasswordHelper;
    private PgpHelper mPgpHelper;
    private PGPPrivateKey mPrivateKey;

    private List<Secret> mSecrets;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPgpHelper = new PgpHelper(getActivity());
        mPasswordHelper = new PasswordHelper(getActivity(), this);
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

    private void cleanUp() {
        setListAdapter(null);
        mPrivateKey = null;
        mSecrets = null;
    }

    @Override
    public void onRefresh() {
//        loadSecrets();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void loadSecrets() {
        Api api = ApiService.createApiService(getActivity());

        BaseParams params = new BaseParams(mPgpHelper.getPublicKeyString());
        Call<ApiResponse> call = api.secrets(params);
        call.enqueue(this);
    }

    @Override
    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
        if (response.isSuccess()) {
            String decryptedJson = response.body().decryptResponseData(getActivity(), mPrivateKey);
            mSecrets = JsonHelper.convertJsonToSecretList(decryptedJson);
            SecretsAdapter secretsAdapter = new SecretsAdapter(getActivity(), android.R.layout.simple_list_item_2, mSecrets);
            setListAdapter(secretsAdapter);
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onFailure(Call<ApiResponse> call, Throwable t) {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onCorrectPassword(PGPPrivateKey privateKey) {
        Log.d("SECRETS", "Correct!");
        mPrivateKey = privateKey;
//        loadSecrets();
    }

    @Override
    public void onIncorrectPassword() {
        Log.d("SECRETS", "Incorrect!");
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Secret secret = mSecrets.get(position);

        // Show secret dialog with copy function.
    }
}
