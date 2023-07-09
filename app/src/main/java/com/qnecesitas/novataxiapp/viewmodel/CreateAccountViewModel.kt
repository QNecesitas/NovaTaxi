package com.qnecesitas.novataxiapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qnecesitas.novataxiapp.auxiliary.Constants
import com.qnecesitas.novataxiapp.network.UserDataSourceNetwork
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateAccountViewModel : ViewModel() {

    //Network Data Source
    private var userDataSourceNetwork: UserDataSourceNetwork = UserDataSourceNetwork()

    //Progress state
    enum class StateConstants {LOADING, SUCCESS, ERROR,DUPLICATED}
    private val _state = MutableLiveData<StateConstants>()
    val state: LiveData<StateConstants> get() = _state



    fun addNewAccountUser(
        name :String,
        email : String,
        phone: String,
        password : String
    )
    {
        _state.value = StateConstants.LOADING
        val call = userDataSourceNetwork.addUserInformation(
            Constants.PHP_TOKEN,
            name,
            email,
            phone,
            password
        )
        getResponseNewAccountUser(call)
    }


    private fun getResponseNewAccountUser(call: Call<String>){
        call.enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String> ,
                response: Response<String>
            ) {
                if (response.isSuccessful) {
                    when (response.body()) {
                        "Exist" -> {
                            _state.value = StateConstants.DUPLICATED
                        }
                        "Success" -> {
                            _state.value = StateConstants.SUCCESS
                        }
                        else -> {
                            _state.value = StateConstants.ERROR
                        }
                    }
                } else {
                    _state.value = StateConstants.ERROR

                }
            }

            override fun onFailure(call: Call<String> , t: Throwable) {
                _state.value = StateConstants.ERROR
            }
        })  }

}


class CreateAccountViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateAccountViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateAccountViewModel() as T
        }
        throw IllegalArgumentException("Unknown viewModel class")
    }
}