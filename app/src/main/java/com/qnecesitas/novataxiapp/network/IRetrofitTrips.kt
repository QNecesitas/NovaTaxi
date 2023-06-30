package com.qnecesitas.novataxiapp.network

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface IRetrofitTrips {


    @FormUrlEncoded
    @POST("AddTrip.php")
    fun addTrip(
        @Field("token") token: String,
        @Field("fk_driver") fk_driver: String,
        @Field("fk_user") fk_user: String,
        @Field("price") price:Int,
        @Field("distance") distance: Double,
        @Field("date") date: String,
        @Field("latDest") latDest: Double,
        @Field("longDest") longDest: Double,
        @Field("latOri") latOri: Double,
        @Field("longOri") longOri: Double,
        @Field("userPhone") userPhone: String
    ): Call<String>


}