package com.qnecesitas.novataxiapp.network

import com.qnecesitas.novataxiapp.model.Driver
import com.qnecesitas.novataxiapp.model.User
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface IRetrofitUsers {


    @GET("FetchUserInformation.php")
    fun getUserInformation(
        @Query("token") token: String,
        @Query("version") version: String,
        @Query("email") email: String,
        @Query("password") password: String
    ): Call<List<User>>

    @FormUrlEncoded
    @POST("AddUserInformation.php")
    fun addUserInformation(
        @Field("token") token: String,
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("phone") phone:String,
        @Field("password") password: String,
    ): Call<String>



    @GET("SendRecoverPetition.php")
    fun sendRecoverPetition(
        @Query("token") token: String,
        @Query("email") email: String
    ): Call<String>

    @GET("FetchUserInformationAll.php")
    fun getUserInformationAll(
        @Query("token") token: String,
        @Query("email") email: String
    ): Call<List<User>>

    @FormUrlEncoded
    @POST("UpdateUserInformation.php")
    fun updateUser(
        @Field("token") token: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("phone") phone: String
    ):Call<String>

}