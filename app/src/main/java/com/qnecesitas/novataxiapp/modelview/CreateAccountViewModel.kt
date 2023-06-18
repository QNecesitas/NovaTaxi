package com.qnecesitas.novataxiapp.modelview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CreateAccountViewModel: ViewModel() {

    fun getResponseLogin(email: String, passwor: String){

    }



}
class CreateAccountViewModelFactory() : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateAccountViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateAccountViewModel() as T
        }
        throw IllegalArgumentException("Unknown viewModel class")
    }
}