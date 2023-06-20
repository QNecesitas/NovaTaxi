package com.qnecesitas.novataxiapp.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qnecesitas.novataxiapp.auxiliary.Constants
import com.qnecesitas.novataxiapp.auxiliary.UserAccount
import com.qnecesitas.novataxiapp.model.User
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

    //Network Data Source
    private var userDataSourceNetwork: UserDataSourceNetwork = UserDataSourceNetwork()



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
}

class LoginViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel() as T
        }
        throw IllegalArgumentException("Unknown viewModel class")
    }
}