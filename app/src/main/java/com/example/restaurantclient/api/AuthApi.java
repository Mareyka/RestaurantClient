package com.example.restaurantclient.api;

import com.example.restaurantclient.models.AuthResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;


public interface AuthApi {

    @POST("auth/register")
    @FormUrlEncoded
    Call<AuthResponse> register(
            @Field("login") String login,
            @Field("password") String password,
            @Field("fullName") String fullName,
            @Field("email") String email
    );

    @POST("auth/login")
    @FormUrlEncoded
    Call<AuthResponse> login(
            @Field("login") String login,
            @Field("password") String password
    );

    @GET("auth/check")
    Call<AuthResponse> checkAuth();

    @GET("auth/logout")
    Call<AuthResponse> logout();
}