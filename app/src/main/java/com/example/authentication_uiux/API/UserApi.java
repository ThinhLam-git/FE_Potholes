package com.example.authentication_uiux.API;

import com.example.authentication_uiux.models.user.ChangePasswordRequest;
import com.example.authentication_uiux.models.user.ChangePasswordResponse;
import com.example.authentication_uiux.models.user.ForgotPasswordRequest;
import com.example.authentication_uiux.models.user.ForgotPasswordResponse;
import com.example.authentication_uiux.models.user.LoginRequest;
import com.example.authentication_uiux.models.user.LoginResponse;
import com.example.authentication_uiux.models.user.SignUpRequest;
import com.example.authentication_uiux.models.user.SignUpResponse;
import com.example.authentication_uiux.models.user.VerifyOtpRequest;
import com.example.authentication_uiux.models.user.VerifyOtpResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PATCH;

public interface UserApi {
    @POST("/user/login")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

    @POST("/user/signup")
    Call<SignUpResponse> signUpUser(@Body SignUpRequest signUpRequest);

    @POST("/user/verify")
    Call<VerifyOtpResponse> verifyOtp(@Body VerifyOtpRequest verifyOtpRequest);

    @PATCH("/user/forgot")
    Call<ForgotPasswordResponse> forgotPassword(@Body ForgotPasswordRequest forgotPasswordRequest);

    @PATCH("/user/change")
    Call<ChangePasswordResponse> changePassword(@Body ChangePasswordRequest changePasswordRequest);

    @POST("/logout")
    Call<Void> logoutUser();
}
