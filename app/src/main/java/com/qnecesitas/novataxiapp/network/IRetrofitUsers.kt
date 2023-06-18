package com.qnecesitas.novataxiapp.network

import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET

interface IRetrofitUsers {

    @FormUrlEncoded
    @GET("FetchUserInformation.php")
    fun getUserInformation(
        email: String, password: String){
    }

}