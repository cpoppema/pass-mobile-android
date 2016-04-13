package mobile.android.pass;

import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import mobile.android.pass.api.Api;
import mobile.android.pass.api.ApiResponse;
import mobile.android.pass.api.ApiService;
import mobile.android.pass.api.BaseParams;
import mobile.android.pass.pgp.PgpHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements Callback<ApiResponse> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        String[] menuItems = getResources().getStringArray(R.array.menu_items);
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView drawerList = (ListView) findViewById(R.id.left_drawer);

        drawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, menuItems));

        SecretsFragment fragment = new SecretsFragment();

        getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();

        PgpHelper pgpHelper = new PgpHelper(this);

        if (!PreferenceManager.getDefaultSharedPreferences(this).contains("public")) {
            pgpHelper.generateKeyPair("TheKeyName", "TheKeyPassword");
            Log.d("MAIN", pgpHelper.getPublicKeyString());
        } else {
            Log.d("MAIN", "KEY EXISTS");
        }

        Api api = ApiService.createApiService(this);

        BaseParams params = new BaseParams(pgpHelper.getPublicKeyString());
        Call<ApiResponse> call = api.secrets(params);
        call.enqueue(this);
    }

    @Override
    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
        if (response.isSuccess()) {
            ApiResponse apiResponse = response.body();

//            ((TextView) findViewById(R.id.main_text)).setText(apiResponse.decryptResponseData(this, "TheKeyPassword"));
        }

    }

    @Override
    public void onFailure(Call<ApiResponse> call, Throwable t) {

    }
}
