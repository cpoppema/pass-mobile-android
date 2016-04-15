package mobile.android.pass.api;

import android.content.Context;

import com.google.gson.GsonBuilder;

import mobile.android.pass.utils.Storage;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Function to create a api service to call the endpoints in api.
 */
public class ApiService {

    public static String getApiUrl(Context context) {

        String url = new Storage(context).getServerAddress();

        if (url.equals("")) {
            url = "http://localhost";
        }
        return url;
    }

    /**
     * Create a api service for the url stored in the storage.
     * @param context
     * @return
     */
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
