package com.qnecesitas.novataxiapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qnecesitas.novataxiapp.auxiliary.Constants
import com.qnecesitas.novataxiapp.model.User
import com.qnecesitas.novataxiapp.network.UserDataSourceNetwork
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserSettingsViewModel:ViewModel() {

    //List driver
    private val _listUser = MutableLiveData<MutableList<User>>()
    val listUser: LiveData<MutableList<User>> get() = _listUser

    //Progress state
    enum class StateConstants { LOADING, SUCCESS, ERROR }

    private val _state = MutableLiveData<StateConstants>()
    val state: LiveData<StateConstants> get() = _state

    //Network Data Source
    private var userDataSourceNetwork: UserDataSourceNetwork = UserDataSourceNetwork()

    //get User Information
    fun getUserInformationAll(email:String) {
        _state.value = StateConstants.LOADING
        val call = userDataSourceNetwork.getUserInformationAll(
            Constants.PHP_TOKEN,
            email
        )
        getResponseUser(call)
    }

    private fun getResponseUser(call: Call<List<User>>) {
        call.enqueue(object : Callback<List<User>> {
            override fun onResponse(
                call: Call<List<User>> ,
                response: Response<List<User>>
            ) {
                if (response.isSuccessful) {
                    _state.value = StateConstants.SUCCESS
                    _listUser.value = response.body()?.toMutableList()

                } else {
                    _state.value = StateConstants.ERROR
                }
            }

            override fun onFailure(call: Call<List<User>> , t: Throwable) {
                _state.value = StateConstants.ERROR
            }
        })
    }

    fun updateUser(email:String,phone:String,password:String){
        _state.value = StateConstants.LOADING
        val call = userDataSourceNetwork.updateUser(Constants.PHP_TOKEN,email,password,phone)
        getResponseUpdateUser(call)
    }

    private fun getResponseUpdateUser(call: Call<String>) {
        call.enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String> ,
                response: Response<String>
            ) {
                if (response.isSuccessful) {
                    _state.value = StateConstants.SUCCESS
                } else {
                    _state.value = StateConstants.ERROR
                }
            }

            override fun onFailure(call: Call<String> , t: Throwable) {
                _state.value = StateConstants.ERROR
            }
        })
    }


}

class UserSettingsViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserSettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserSettingsViewModel() as T
        }
        throw IllegalArgumentException("Unknown viewModel class")
    }
}