package com.qnecesitas.novataxiapp.network

import com.qnecesitas.novataxiapp.data.ModelUser
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.ArrayList

interface IRetrofitUsers {

    @FormUrlEncoded
    @POST("UpdateAccount.php")
    fun updateAccount(
        @Field("token") token: String,
        @Field("password") password: String,
        @Field("user") user: String
    ) : Call<String>


    @GET("FetchOrdersD.php")
    fun fetchOrders(
        @Query("token") token: String,
        @Query("version") version: String,
        @Query("email") email: String,
        @Query("password") password: String
    ): Call<ArrayList<ModelUser>>


}