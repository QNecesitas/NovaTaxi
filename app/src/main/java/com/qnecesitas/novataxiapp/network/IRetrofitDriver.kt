package com.qnecesitas.novataxiapp.network

import com.qnecesitas.novataxiapp.model.Driver
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface IRetrofitDriver {

    @GET("FetchDriverInformation.php")
    fun getDriverInformation(
        @Query("token") token: String,
        @Query("email") email: String
    ): Call<List<Driver>>

    @GET("FetchDriverSmallInformation.php")
    fun getDriver(
        @Query("token") token: String,
    ): Call<List<Driver>>

    @FormUrlEncoded
    @POST("RateDriver.php")
    fun rateDriver(
        @Field("token") token: String,
        @Field("rate") rate: Int,
        @Field("driver ") driver: String
    ): Call<String>


    @GET("FetchDriver.php")
    fun getDriverForNavigation(
        @Query("token") token: String,
        @Query("email") email: String,
    ): Call<Driver>

}