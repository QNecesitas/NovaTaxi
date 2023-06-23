package com.qnecesitas.novataxiapp.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qnecesitas.novataxiapp.auxiliary.Constants
import com.qnecesitas.novataxiapp.auxiliary.UserAccount
import com.qnecesitas.novataxiapp.model.User
import com.qnecesitas.novataxiapp.network.AuxiliaryDataSourceNetwork
import com.qnecesitas.novataxiapp.network.UserDataSourceNetwork
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : ViewModel() {



    //List user
    private val _listUsers = MutableLiveData<MutableList<User>>()
    val listUser: LiveData<MutableList<User>> get() = _listUsers

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
    fun getUserWithPassword(email: String, password: String) {
        _state.value = StateConstants.LOADING
        val call = userDataSourceNetwork.getUserInformation(
            Constants.PHP_TOKEN,
            Constants.APP_VERSION,
            email,
            password
        )
        getResponseUserWithPassword(call)
    }

    //Get the response about the user info
    private fun getResponseUserWithPassword(call: Call<List<User>>){
        call.enqueue(object : Callback<List<User>> {
            override fun onResponse(
                call: Call<List<User>>,
                response: Response<List<User>>
            ) {
                if (response.isSuccessful) {
                    _state.value = StateConstants.SUCCESS
                    _listUsers.value = response.body()?.toMutableList()
                } else {
                    _state.value = StateConstants.ERROR
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                _state.value = StateConstants.ERROR
            }
        })
    }

    //Save the info
    fun saveUserInfo(user: User?, context: Context){
        user?.let { UserAccount.setUserInfo(it, context) }
    }

    //Send recover petition
    fun sendRecoverPetition(email: String){
        _stateRecover.value = StateConstants.SUCCESS
        val call = userDataSourceNetwork.sendRecoverPetition(
            Constants.PHP_TOKEN,
            Constants.APP_VERSION,
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
                    _stateRecover.value = StateConstants.SUCCESS
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
                        Log.i("TEST", "OK")
                        _stateVersion.value = StateConstants.SUCCESS
                    }else{
                        _versionResponse.value = result
                    }
                } else {
                    _stateVersion.value = StateConstants.ERROR
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                _stateRecover.value = StateConstants.ERROR
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