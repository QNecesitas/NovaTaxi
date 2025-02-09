package com.qnecesitas.novataxiapp.network

import com.qnecesitas.novataxiapp.auxiliary.Constants
import com.qnecesitas.novataxiapp.model.User
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserDataSourceNetwork : IRetrofitUsers {

    private val retrofit : Retrofit = Retrofit.Builder()
        .baseUrl(Constants.PHP_FILES)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val productApi: IRetrofitUsers = retrofit.create(IRetrofitUsers::class.java)


    override fun getUserExist(
        token: String,
        email: String,
        password: String
    ): Call<String> {
        return productApi.getUserExist(token, email, password)
    }

    override fun getUserInformationAll(token: String , email: String): Call<List<User>> {
        return productApi.getUserInformationAll(token,email)
    }

    override fun updateUser(
        token: String ,
        email: String ,
        password: String ,
        phone: String
    ): Call<String> {
        return productApi.updateUser(token, email, password, phone)
    }

    override fun deleteUser(token: String , email: String): Call<String> {
        return productApi.deleteUser(token, email)
    }

    override fun sendRecoverPetition(
        token: String,
        email: String
    ): Call<String> {
        return productApi.sendRecoverPetition(token, email)
    }

    override fun addUserInformation(
        token: String ,
        name: String ,
        email: String ,
        phone: String ,
        password: String
    ): Call<String> {
        return productApi.addUserInformation(token,name,email,phone,password)
    }

}