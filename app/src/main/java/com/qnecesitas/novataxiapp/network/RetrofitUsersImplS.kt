package com.qnecesitas.novataxiapp.network

import com.qnecesitas.novataxiapp.auxiliary.Constants
import com.qnecesitas.novataxiapp.data.ModelUser
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.ArrayList

class RetrofitUsersImplS: IRetrofitUsers {

    private val retrofit : Retrofit = Retrofit.Builder()
        .baseUrl(Constants.PHP_FILES)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val productApi: IRetrofitUsers = retrofit.create(IRetrofitUsers::class.java)

    override fun updateAccount(token: String, password: String, user: String): Call<String> {
        return productApi.updateAccount(token, password,user)
    }


    override fun fetchOrders(token: String,version: String,email: String, password: String): Call<ArrayList<ModelUser>> {
        return productApi.fetchOrders(token,version,email,password)
    }


}