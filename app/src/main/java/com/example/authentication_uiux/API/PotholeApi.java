package com.example.authentication_uiux.API;

import com.example.authentication_uiux.models.PotholeData;
import com.example.authentication_uiux.models.RankData;
import com.example.authentication_uiux.models.pothhole.PotholeStatistics;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface PotholeApi {
    @POST("api/potholes/addPothole")
    Call<Void> addPothole(@Body PotholeData potholeData);

    @GET("api/potholes/total")
    Call<PotholeStatistics> getPotholeStatistics();

    @GET("/api/potholes/getPotholes")
    Call<List<PotholeData>> getPotholes();

    @PUT("api/potholes/updatePotholeStatus")
    Call<Void> updatePotholeStatus(@Body PotholeData potholeData);

    @GET("api/potholes/ranking")
    Call<List<RankData>> getUserRankings();
}