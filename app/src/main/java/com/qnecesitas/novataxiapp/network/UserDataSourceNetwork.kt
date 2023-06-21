package com.qnecesitas.novataxiapp.network

import com.qnecesitas.novataxiapp.auxiliary.Constants
import com.qnecesitas.novataxiapp.model.User
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserDataSourceNetwork : IRetrofitUsers {

    private val retrofit : Retrofit = Retrofit.Builder()
        .baseUrl(Constants.PHP_FILES)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val productApi: IRetrofitUsers = retrofit.create(IRetrofitUsers::class.java)


    override fun getUserInformation(
        token: String,
        version: Double,
        email: String,
        password: String
    ): Call<List<User>> {
        return productApi.getUserInformation(token, version, email, password)
    }

    override fun sendRecoverPetition(
        token: String,
        version: Double,
        email: String
    ): Call<String> {
        return productApi.sendRecoverPetition(token,version, email)
    }
}