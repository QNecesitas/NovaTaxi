package com.qnecesitas.novataxiapp.viewmodel

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

class DriverViewModel : ViewModel() {

    //List user
    private val _listDriver = MutableLiveData<MutableList<Driver>>()
    val listDriver: LiveData<MutableList<Driver>> get() = _listDriver

    //Progress state
    enum class StateConstants { LOADING, SUCCESS, ERROR }

    private val _state = MutableLiveData<StateConstants>()
    val state: LiveData<StateConstants> get() = _state

    //Network Data Source
    private var driverDataSourceNetwork: DriverDataSourceNetwork = DriverDataSourceNetwork()


    /*
    Call and enqueue
     */
    //Send InfoDriver
    fun getDriver(email: String) {
        _state.value = StateConstants.LOADING
        val call = driverDataSourceNetwork.getDriverInformation(
            Constants.PHP_TOKEN,
            email
        )
        getResponseInfoDriver(call)
    }

    //Get the response about the Driver info
    private fun getResponseInfoDriver(call: Call<List<Driver>>) {
        call.enqueue(object : Callback<List<Driver>> {
            override fun onResponse(
                call: Call<List<Driver>>,
                response: Response<List<Driver>>
            ) {
                if (response.isSuccessful) {
                    _state.value = StateConstants.SUCCESS
                    _listDriver.value = response.body()?.toMutableList()
                } else {
                    _state.value = StateConstants.ERROR
                }
            }

            override fun onFailure(call: Call<List<Driver>>, t: Throwable) {
                _state.value = StateConstants.ERROR
            }
        })
    }


    /*
    Methods
     */


}

class DriverViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DriverViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DriverViewModel() as T
        }
        throw IllegalArgumentException("Unknown viewModel class")
    }
}