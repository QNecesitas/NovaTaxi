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
    fun getDriverProv(
        @Query("token") token: String,
        @Query("prov") prov: Int
    ): Call<List<Driver>>


}