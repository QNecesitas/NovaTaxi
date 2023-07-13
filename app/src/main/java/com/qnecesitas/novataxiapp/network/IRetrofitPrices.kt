package com.qnecesitas.novataxiapp.network


import com.qnecesitas.novataxidriver.model.Prices
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface IRetrofitPrices {

    @GET("FetchPricesInformation.php")
    fun getPricesInformation(
        @Query("token") token: String ,
        @Query("id") id: Int
    ): Call<List<Prices>>

    @FormUrlEncoded
    @POST("UpdatePriceKm.php")
    fun updatePriceKm(
        @Field("token") token: String ,
        @Field("id") id: Int ,
        @Field("priceKm") priceKm: Int
    ):Call<String>

    @FormUrlEncoded
    @POST("UpdatePriceDelay.php")
    fun updatePriceDelay(
        @Field("token") token: String ,
        @Field("id") id: Int,
        @Field("priceDelay") priceDelay: Int
    ):Call<String>

    @FormUrlEncoded
    @POST("UpdateDelayTime.php")
    fun updateDelayTime(
        @Field("token") token: String ,
        @Field("id") id: Int ,
        @Field("delayTime") delayTime: Int
    ):Call<String>

    @FormUrlEncoded
    @POST("UpdateDescPercent.php")
    fun updateDescPercent(
        @Field("token") token: String ,
        @Field("id") id: Int ,
        @Field("descPercent") descPercent: Int
    ):Call<String>

}