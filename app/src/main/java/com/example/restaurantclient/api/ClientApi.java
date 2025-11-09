package com.example.restaurantclient.api;

import com.example.restaurantclient.models.Client;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import java.util.List;

 // использование Retrofit для взаимодействия с REST API

public interface ClientApi {

    @GET("clients")                          // HTTP GET метод к endpoint /clients
    Call<List<Client>> getAllClients();


    @POST("clients")
    @FormUrlEncoded
    Call<Void> createClient(
            @Field("fullName") String fullName,   // Параметр в теле запроса
            @Field("contacts") String contacts
    );


    @PUT("clients")
    Call<Void> updateClient(
            @Query("id") int id,
            @Query("fullName") String fullName,
            @Query("contacts") String contacts
    );


    @DELETE("clients")
    Call<Void> deleteClient(@Query("id") int id);
}