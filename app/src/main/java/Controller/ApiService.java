package Controller;

import Model.AuthResponse;
import Model.LoginRequest;
import Model.RegisterRequest;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.Call;

public interface ApiService {
    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);
}
