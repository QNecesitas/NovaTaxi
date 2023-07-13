package com.qnecesitas.novataxiapp.network

import android.util.Log
import com.qnecesitas.novataxiapp.auxiliary.Constants
import com.qnecesitas.novataxiapp.model.Driver
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TripsDataSourceNetwork: IRetrofitTrips {

    private val retrofit : Retrofit = Retrofit.Builder()
        .baseUrl(Constants.PHP_FILES)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val productApi: IRetrofitTrips = retrofit.create(IRetrofitTrips::class.java)


    override fun addTrip(
        token: String,
        fk_driver: String,
        fk_user: String,
        price: Int,
        distance: Double,
        date: String,
        latDest: Double,
        longDest: Double,
        latOri: Double,
        longOri: Double,
        userPhone: String,
        typeCar: String
    ): Call<String> {
        return productApi.addTrip(
            token,
            fk_driver,
            fk_user,
            price,
            distance,
            date,
            latDest,
            longDest,
            latOri,
            longOri,
            userPhone,
            typeCar
        )
    }

    override fun deleteTrip(token: String, fk_user: String): Call<String> {
        return productApi.deleteTrip(token, fk_user)
    }

    override fun fetchStateTrip(token: String, fk_user: String): Call<String> {
        return productApi.fetchStateTrip(token, fk_user)
    }


}