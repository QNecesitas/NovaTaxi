package com.qnecesitas.novataxiapp.network

import com.qnecesitas.novataxiapp.auxiliary.Constants
import com.qnecesitas.novataxiapp.model.User
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuxiliaryDataSourceNetwork : IRetrofitAuxiliary {

    private val retrofit : Retrofit = Retrofit.Builder()
        .baseUrl(Constants.PHP_FILES)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val productApi: IRetrofitAuxiliary = retrofit.create(IRetrofitAuxiliary::class.java)
    override fun fetchVersion(version: String): Call<String> {
        return productApi.fetchVersion(version)
    }


}