package com.qnecesitas.novataxiapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qnecesitas.novataxiapp.auxiliary.Constants
import com.qnecesitas.novataxiapp.model.Driver
import com.qnecesitas.novataxiapp.network.DriverDataSourceNetwork
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.pow
import kotlin.math.sqrt

class MapHomeViewModel: ViewModel() {

    //List user
    private val _listSmallDriver = MutableLiveData<MutableList<Driver>>()
    val listSmallDriver: LiveData<MutableList<Driver>> get() = _listSmallDriver

    //Progress state
    enum class StateConstants { LOADING, SUCCESS, ERROR }

    private val _state = MutableLiveData<StateConstants>()
    val state: LiveData<StateConstants> get() = _state

    //Network Data Source
    private var driverDataSourceNetwork: DriverDataSourceNetwork = DriverDataSourceNetwork()

    //LatitudeClient
    private val _latitudeClient = MutableLiveData<Double>()
    val latitudeClient: LiveData<Double> get() = _latitudeClient

    //LongitudeClient
    private val _longitudeClient = MutableLiveData<Double>()
    val longitudeClient: LiveData<Double> get() = _longitudeClient

    //LatitudeDestiny
    private val _latitudeDestiny = MutableLiveData<Double>()
    val latitudeDestiny: LiveData<Double> get() = _latitudeDestiny

    //LongitudeDestiny
    private val _longitudeDestiny = MutableLiveData<Double>()
    val longitudeDestiny: LiveData<Double> get() = _longitudeDestiny


    /*
      Call and enqueue
       */
    //Bring InfoDriver
    fun getDriverProv(latUser: Double, longUser: Double) {
        _state.value = StateConstants.LOADING

        //Call
        val call = driverDataSourceNetwork.getDriver(
            Constants.PHP_TOKEN,
        )
        getResponseInfoDriverProv(call,latUser,longUser)
    }

    //Get the response about the Driver info
    private fun getResponseInfoDriverProv(call: Call<List<Driver>>,latUser: Double, longUser: Double) {
        call.enqueue(object : Callback<List<Driver>> {
            override fun onResponse(
                call: Call<List<Driver>>,
                response: Response<List<Driver>>
            ) {
                if (response.isSuccessful) {
                    _state.value = StateConstants.SUCCESS
                    filterDriver(response.body()?.toMutableList(),latUser,longUser)
                    Log.e("XXXXXX", listSmallDriver.value.toString())
                } else {
                    _state.value = StateConstants.ERROR
                }
            }

            override fun onFailure(call: Call<List<Driver>>, t: Throwable) {
                _state.value = StateConstants.ERROR
            }
        })
    }


    private fun filterDriver(alDriver: MutableList<Driver>?,latUser: Double, longUser: Double){
        val alResult = alDriver?.filter {
            it.maxDist < calDist(latUser ,longUser, it.lat, it.long)
        }?.toMutableList()

        _listSmallDriver.value = alResult
    }


    //Calculate Distance
    private fun calDist(latUser: Double, longUser: Double, latCar:Double, longCar:Double): Double{
        return  sqrt((latCar - latUser).pow(2.0) + (longCar - longUser).pow(2.0))
    }

    //SetLocations
    fun setLatitudeClient(lat: Double){
        _latitudeClient.value = lat
    }
    fun setLongitudeClient(long: Double){
        _longitudeClient.value = long
    }
    fun setLatitudeDestiny(lat: Double){
        _latitudeDestiny.value = lat
    }
    fun setLongitudeDestiny(long: Double){
        _longitudeDestiny.value = long
    }

}


class MapHomeViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapHomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapHomeViewModel() as T
        }
        throw IllegalArgumentException("Unknown viewModel class")
    }
}