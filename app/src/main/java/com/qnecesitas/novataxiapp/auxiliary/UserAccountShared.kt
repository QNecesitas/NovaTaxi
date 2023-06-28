package com.qnecesitas.novataxiapp.auxiliary

import android.content.Context
import android.content.SharedPreferences
import com.mapbox.geojson.Point
import com.qnecesitas.novataxiapp.model.User

class UserAccountShared {
    companion object{

        //Shared
        var sharedPreferences: SharedPreferences? = null
        var sharedEditor: SharedPreferences.Editor? = null


        // All the user info since the first time
        fun setUserInfo(user: User, context: Context){
            sharedPreferences = context.getSharedPreferences("NovaTaxi", 0);
            sharedEditor = sharedPreferences?.edit()
            sharedEditor?.putString("userEmail",user.email)
            sharedEditor?.putString("userPassword",user.password)
            sharedEditor?.putString("userName",user.name)
            sharedEditor?.putString("userPhone",user.phone)
            sharedEditor?.putString("userState",user.state)
            sharedEditor?.apply()
            setIsLogged(context, true)
        }

        fun getUserInfo(context: Context): User?{
            if(getIsLogged(context)) {
                sharedPreferences = context.getSharedPreferences("NovaTaxi", 0);
                val email = sharedPreferences?.getString("userEmail", null)
                val password = sharedPreferences?.getString("userPassword", null)
                val name = sharedPreferences?.getString("userName", null)
                val phone = sharedPreferences?.getString("userPhone", null)
                val state = sharedPreferences?.getString("userState", null)

                return User(
                    email!!,
                    phone!!,
                    name!!,
                    state!!,
                    password!!
                )

            }else return null
        }


        //Knowing is logged
        private fun getIsLogged(context: Context): Boolean {
            sharedPreferences = context.getSharedPreferences("NovaTaxi", 0)
            return sharedPreferences?.getBoolean("isLogged", false) == true
        }

        private fun setIsLogged(context: Context, isLogged: Boolean){
            sharedPreferences = context.getSharedPreferences("NovaTaxi", 0);
            sharedEditor = sharedPreferences?.edit()
            sharedEditor?.putBoolean("isLogged", isLogged)
            sharedEditor?.apply()
        }


        //Last location for camera map
        fun setLastLocation(context: Context, point: Point){
            sharedPreferences = context.getSharedPreferences("NovaTaxi", 0);
            sharedEditor = sharedPreferences?.edit()
            sharedEditor?.putString("lastLatitudeUser", point.latitude().toString())
            sharedEditor?.putString("lastLongitudeUser", point.longitude().toString())
            sharedEditor?.apply()
        }

        fun getLastLocation(context: Context): Point{
            sharedPreferences = context.getSharedPreferences("NovaTaxi", 0)
            val latitude = sharedPreferences?.getString("lastLatitudeUser", "20.886953")
            val longitude = sharedPreferences?.getString("lastLongitudeUser", "-76.2593")
            return Point.fromLngLat(
                longitude?.toDouble() ?: -76.2593,
                latitude?.toDouble() ?: 20.886953
            )
        }

    }
}