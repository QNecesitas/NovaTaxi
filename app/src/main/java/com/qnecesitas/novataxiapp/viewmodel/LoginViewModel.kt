package com.qnecesitas.novataxiapp.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qnecesitas.novataxiapp.auxiliary.Constants
import com.qnecesitas.novataxiapp.auxiliary.UserAccountShared
import com.qnecesitas.novataxiapp.network.AuxiliaryDataSourceNetwork
import com.qnecesitas.novataxiapp.network.UserDataSourceNetwork
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : ViewModel() {



    //Exist driver
    private val _existDriver = MutableLiveData<String>()
    val existDriver: LiveData<String> get() = _existDriver

    //Progress state
    enum class StateConstants {LOADING, SUCCESS, ERROR}
    private val _state = MutableLiveData<StateConstants>()
    val state: LiveData<StateConstants> get() = _state

    //Progress state recover
    private val _stateRecover = MutableLiveData<StateConstants>()
    val stateRecover: LiveData<StateConstants> get() = _stateRecover

    //Progress state version
    private val _stateVersion = MutableLiveData<StateConstants>()
    val stateVersion: LiveData<StateConstants> get() = _stateVersion

    //Version response
    private val _versionResponse = MutableLiveData<String>()
    val versionResponse: LiveData<String> get() = _versionResponse

    //Network Data Source
    private var userDataSourceNetwork: UserDataSourceNetwork = UserDataSourceNetwork()
    private var auxiliaryDataSourceNetwork: AuxiliaryDataSourceNetwork = AuxiliaryDataSourceNetwork()



    //Send request for userInfo
    fun getIsValidAccount(email: String, password: String) {
        _state.value = StateConstants.LOADING
        val call = userDataSourceNetwork.getUserExist(
            Constants.PHP_TOKEN,
            email,
            password
        )
        getResponseIsValidAccount(call)
    }

    //Get the response about the user info
    private fun getResponseIsValidAccount(call: Call<String>){
        call.enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                if (response.isSuccessful) {
                    if(response.body() != null){
                        _state.value = StateConstants.SUCCESS
                        _existDriver.value = response.body()
                    }else{
                        _state.value = StateConstants.ERROR
                    }
                } else {
                    _state.value = StateConstants.ERROR
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                _state.value = StateConstants.ERROR
            }
        })
    }

    //Save the info
    fun saveUserInfo(email: String, context: Context){
        UserAccountShared.setUserEmail(email, context)
    }

    //Send recover petition
    fun sendRecoverPetition(email: String){
        val call = userDataSourceNetwork.sendRecoverPetition(
            Constants.PHP_TOKEN,
            email
        )
        getResponseRecoverPetition(call)
    }

    //Get the response is operation ok
    private fun getResponseRecoverPetition(call: Call<String>){
        call.enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                if (response.isSuccessful) {
                    if(response.body() != null){
                            _stateRecover.value = StateConstants.SUCCESS
                    }else{
                        _stateRecover.value = StateConstants.ERROR
                    }
                } else {
                    _stateRecover.value = StateConstants.ERROR
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                _stateRecover.value = StateConstants.ERROR
            }
        })
    }


    //Fetch APP_Version
    fun getAppVersion(){
        val call = auxiliaryDataSourceNetwork.fetchVersion(
            Constants.APP_VERSION
        )
        getResponseVersion(call)
    }

    //Get the response is version ok
    private fun getResponseVersion(call: Call<String>){
        call.enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                if (response.isSuccessful) {
                    val result = response.body()
                    if(result == "Success") {
                        _stateVersion.value = StateConstants.SUCCESS
                    }else{
                        _versionResponse.value = result
                    }
                } else {
                    _stateVersion.value = StateConstants.ERROR
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                _stateVersion.value = StateConstants.ERROR
            }
        })
    }


}

class LoginViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel() as T
        }
        throw IllegalArgumentException("Unknown viewModel class")
    }
}