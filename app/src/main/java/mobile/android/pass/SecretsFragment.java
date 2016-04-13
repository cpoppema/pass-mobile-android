package mobile.android.pass;

import android.app.ListFragment;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SimpleAdapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
        implements SwipeRefreshLayout.OnRefreshListener, Callback<ApiResponse> {
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private PgpHelper mPgpHelper;


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

        mPgpHelper = new PgpHelper(getActivity());
    }

    @Override
    public void onRefresh() {
        Api api = ApiService.createApiService(getActivity());

        BaseParams params = new BaseParams(mPgpHelper.getPublicKeyString());
        Call<ApiResponse> call = api.secrets(params);
        call.enqueue(this);
    }

    @Override
    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
        if (response.isSuccess()) {
            String decryptedJson = response.body().decryptResponseData(getActivity(), "TheKeyPassword");
            List<Secret> secrets = JsonHelper.convertJsonToSecretList(decryptedJson);
            SecretsAdapter secretsAdapter = new SecretsAdapter(getActivity(), android.R.layout.simple_list_item_2, secrets);
            setListAdapter(secretsAdapter);
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onFailure(Call<ApiResponse> call, Throwable t) {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    //    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//
//        String[] menuItems = getResources().getStringArray(R.array.menu_items);
//        setListAdapter(new ArrayAdapter<>(getActivity(), R.layout.drawer_list_item, menuItems));
//
//        return super.onCreateView(inflater, container, savedInstanceState);
//    }
}
