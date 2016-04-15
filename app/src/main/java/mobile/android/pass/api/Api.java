package mobile.android.pass.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Api endpoints.
 */
public interface Api {

    @POST("secrets/")
    Call<SecretsResponse> secrets(@Body BaseParams params);

    @POST("secret/")
    Call<SecretResponse> secret(@Body SecretParams params);
}
