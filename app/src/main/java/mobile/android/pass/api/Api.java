package mobile.android.pass.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by marcov on 13-4-16.
 */
public interface Api {

    @POST("secrets/")
    Call<ApiResponse> secrets(@Body BaseParams params);

    @POST("secret/")
    Call<ApiResponse> secret(@Body SecretParams params);
}