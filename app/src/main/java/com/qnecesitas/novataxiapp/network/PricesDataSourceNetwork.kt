package com.qnecesitas.novataxiapp.network

import com.qnecesitas.novataxiapp.auxiliary.Constants
import com.qnecesitas.novataxidriver.model.Prices
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PricesDataSourceNetwork:IRetrofitPrices {
    private val retrofit : Retrofit = Retrofit.Builder()
        .baseUrl(Constants.PHP_FILES)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val productApi: IRetrofitPrices = retrofit.create(IRetrofitPrices::class.java)


    override fun getPricesInformation(token: String , id: Int): Call<List<Prices>> {
       return productApi.getPricesInformation(token, id)
    }

    override fun updatePriceKm(token: String , id: Int , priceKm: Int): Call<String> {
        return productApi.updatePriceKm(token, id, priceKm)
    }

    override fun updatePriceDelay(token: String , id: Int , priceDelay: Int): Call<String> {
        return productApi.updatePriceDelay(token, id, priceDelay)
    }

    override fun updateDelayTime(token: String , id: Int , delayTime: Int): Call<String> {
        return productApi.updateDelayTime(token, id, delayTime)
    }

    override fun updateDescPercent(token: String , id: Int , descPercent: Int): Call<String> {
        return productApi.updateDescPercent(token, id, descPercent)
    }
}