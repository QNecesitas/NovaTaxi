package com.qnecesitas.novataxiapp.network

import android.util.Log
import com.qnecesitas.novataxiapp.auxiliary.Constants
import com.qnecesitas.novataxiapp.model.Driver
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DriverDataSourceNetwork: IRetrofitDriver {

    private val retrofit : Retrofit = Retrofit.Builder()
        .baseUrl(Constants.PHP_FILES)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val productApi: IRetrofitDriver = retrofit.create(IRetrofitDriver::class.java)


    override fun getDriverInformation(
        token: String,
        email: String
    ): Call<List<Driver>> {
        return productApi.getDriverInformation(token, email)
    }

    override fun getDriver(
        token: String,
    ): Call<List<Driver>> {
        return productApi.getDriver(token)
    }

    override fun rateDriver(token: String, rate: Int, driver: String): Call<String> {
        return productApi.rateDriver(token, rate, driver)
    }

    override fun getDriverForNavigation(token: String, email: String): Call<Driver> {
        return productApi.getDriverForNavigation(token, email)
    }

}