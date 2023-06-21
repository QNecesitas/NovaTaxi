package com.qnecesitas.novataxiapp.network

import com.qnecesitas.novataxiapp.model.User
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface IRetrofitAuxiliary {


    @GET("FetchVersion.php")
    fun fetchVersion(
        @Query("version") version: Double,
    ): Call<String>


}