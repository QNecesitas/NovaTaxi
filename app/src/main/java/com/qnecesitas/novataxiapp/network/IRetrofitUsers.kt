package com.qnecesitas.novataxiapp.network

import com.qnecesitas.novataxiapp.model.User
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface IRetrofitUsers {


    @GET("FetchUserInformation.php")
    fun getUserInformation(
        @Query("token") token: String,
        @Query("version") version: Double,
        @Query("email") email: String,
        @Query("password") password: String
    ): Call<List<User>>


    @GET("SendRecoverPetition.php")
    fun sendRecoverPetition(
        @Query("token") token: String,
        @Query("version") version: Double,
        @Query("email") email: String
    ): Call<String>

}