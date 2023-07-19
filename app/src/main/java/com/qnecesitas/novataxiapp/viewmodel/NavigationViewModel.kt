package com.qnecesitas.novataxiapp.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qnecesitas.novataxiapp.auxiliary.Constants
import com.qnecesitas.novataxiapp.auxiliary.UserAccountShared
import com.qnecesitas.novataxiapp.model.Driver
import com.qnecesitas.novataxiapp.model.Trip
import com.qnecesitas.novataxiapp.network.AuxiliaryDataSourceNetwork
import com.qnecesitas.novataxiapp.network.DriverDataSourceNetwork
import com.qnecesitas.novataxiapp.network.PricesDataSourceNetwork
import com.qnecesitas.novataxiapp.network.TripsDataSourceNetwork
import com.qnecesitas.novataxiapp.network.UserDataSourceNetwork
import com.qnecesitas.novataxidriver.model.Prices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NavigationViewModel : ViewModel() {

    //Driver Latitude
    private val _latitudeDriver = MutableLiveData<Double>()
    val latitudeDriver: LiveData<Double> get() = _latitudeDriver
    fun setLatitudeDriver(latitude: Double){
        _latitudeDriver.value = latitude
    }

    //Driver Longitude
    private val _longitudeDriver = MutableLiveData<Double>()
    val longitudeDriver: LiveData<Double> get() = _longitudeDriver
    fun setLongitudeDriver(longitude: Double){
        _longitudeDriver.value = longitude
    }

    //Driver from Network
    private val _driver = MutableLiveData<Driver>()
    val driver: LiveData<Driver> get() = _driver

    //Route state
    private val _actualTrip = MutableLiveData<Trip>()
    val actualTrip: LiveData<Trip> get() = _actualTrip

    //Prices
    private val _actualPrices = MutableLiveData<List<Prices>>()
    val actualPrices: LiveData<List<Prices>> get() = _actualPrices



    //Obtaining the data
    private var driverDataSourceNetwork: DriverDataSourceNetwork = DriverDataSourceNetwork()
    private var tripsDataSourceNetwork: TripsDataSourceNetwork = TripsDataSourceNetwork()
    private var pricesDataSourceNetwork: PricesDataSourceNetwork = PricesDataSourceNetwork()



    //Converting image asset
    fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }



    fun getDriverPosition(email: String) {
        //Call
        val call = driverDataSourceNetwork.getDriverForNavigation(
            Constants.PHP_TOKEN,
            email
        )
        getResponseInfoDriverProv(call)
    }

    //Get the response about the Driver info
    private fun getResponseInfoDriverProv(call: Call<Driver>) {
        call.enqueue(object : Callback<Driver> {
            override fun onResponse(
                call: Call<Driver>,
                response: Response<Driver>
            ) {
                if (response.isSuccessful) {
                    if(response.body() != null) {
                        _driver.value = response.body()!!
                    }
                }
            }

            override fun onFailure(call: Call<Driver>, t: Throwable) {}
        })
    }




    //Fetch state in the trip
    fun fetchStateInTrip(email: String) {
        //Call
        val call = tripsDataSourceNetwork.fetchStateTrip(
            Constants.PHP_TOKEN,
            email
        )
        getResponseFetchStateInTrip(call)
    }

    //Get the response about the Driver info
    private fun getResponseFetchStateInTrip(call: Call<Trip>) {
        call.enqueue(object : Callback<Trip> {
            override fun onResponse(
                call: Call<Trip>,
                response: Response<Trip>
            ) {
                if (response.isSuccessful) {
                    if(response.body() != null) {
                        _actualTrip.value = response.body()
                    }else{
                        _actualTrip.value = null
                    }
                }
            }

            override fun onFailure(call: Call<Trip>, t: Throwable) {
                _actualTrip.value = null
            }
        })
    }




    //Fetch state in the trip
    fun fetchPrices() {
        //Call
        val call = pricesDataSourceNetwork.getPricesInformation(
            Constants.PHP_TOKEN,
            1
        )
        getResponseFetchPrices(call)
    }

    //Get the response about the Driver info
    private fun getResponseFetchPrices(call: Call<List<Prices>>) {
        call.enqueue(object : Callback<List<Prices>> {
            override fun onResponse(
                call: Call<List<Prices>>,
                response: Response<List<Prices>>
            ) {
                if (response.isSuccessful) {
                    if(response.body() != null) {
                        _actualPrices.value = response.body()
                    }else{
                        _actualPrices.value = null
                    }
                }
            }

            override fun onFailure(call: Call<List<Prices>>, t: Throwable) {
                _actualPrices.value = null
            }
        })
    }


}

class NavigationViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NavigationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NavigationViewModel() as T
        }
        throw IllegalArgumentException("Unknown viewModel class")
    }
}