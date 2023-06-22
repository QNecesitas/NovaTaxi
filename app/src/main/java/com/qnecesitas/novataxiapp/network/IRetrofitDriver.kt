package com.qnecesitas.novataxiapp.network

import com.qnecesitas.novataxiapp.model.Driver
import retrofit2.Call
import retrofit2.http.GET
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


}