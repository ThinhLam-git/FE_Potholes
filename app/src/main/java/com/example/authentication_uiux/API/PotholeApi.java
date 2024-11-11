package com.example.authentication_uiux.API;

import com.example.authentication_uiux.models.PotholeData;

import retrofit2.Call;
import  retrofit2.http.Body;
import retrofit2.http.POST;

public interface PotholeApi {
    @POST("api/potholes/addPothole")
    Call<Void> addPothole(@Body PotholeData potholeData);
}
