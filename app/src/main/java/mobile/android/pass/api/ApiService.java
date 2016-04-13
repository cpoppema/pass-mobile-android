package mobile.android.pass.api;

import android.content.Context;

import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by marcov on 13-4-16.
 */
public class ApiService {

    public static String getApiUrl(Context context) {
        return "http://pass.itstars.nl/";
    }

    public static Api createApiService(Context context) {
        Retrofit.Builder builder = new Retrofit.Builder();

        builder.baseUrl(getApiUrl(context))
                .client(new OkHttpClient())
                .addConverterFactory(
                        GsonConverterFactory.create(new GsonBuilder().serializeNulls().create()));

        Retrofit retrofit = builder.build();

        return retrofit.create(Api.class);
    }
}
