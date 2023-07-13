package com.qnecesitas.novataxiapp.auxiliary

import android.content.Context
import android.content.SharedPreferences
import com.mapbox.geojson.Point
import com.qnecesitas.novataxiapp.model.Driver

class UserAccountShared {
    companion object{

        //Shared
        private var sharedPreferences: SharedPreferences? = null
        private var sharedEditor: SharedPreferences.Editor? = null


        fun getUserEmail(context: Context): String?{
            if(sharedPreferences == null) {
                sharedPreferences = context.getSharedPreferences("NovaTaxiAppUser", 0)
            }
            return  sharedPreferences?.getString("userEmail", null)
        }

        fun setUserEmail(email: String?, context: Context){
            if(sharedPreferences == null) {
                sharedPreferences = context.getSharedPreferences("NovaTaxiAppUser", 0)
            }
            if(sharedEditor == null) {
                sharedEditor = sharedPreferences?.edit()
            }
            sharedEditor?.putString("userEmail",email)
            sharedEditor?.apply()
        }




        //Last location for camera map
        fun setLastLocation(context: Context, point: Point){
            if(sharedPreferences == null) {
                sharedPreferences = context.getSharedPreferences("NovaTaxiAppUser", 0)
            }
            if(sharedEditor == null){
                sharedEditor = sharedPreferences?.edit()
            }
            sharedEditor?.putString("lastLatitudeUser", point.latitude().toString())
            sharedEditor?.putString("lastLongitudeUser", point.longitude().toString())
            sharedEditor?.apply()
        }

        fun getLastLocation(context: Context): Point {
            if(sharedPreferences == null) {
                sharedPreferences = context.getSharedPreferences("NovaTaxiAppUser", 0)
            }
            val latitude = sharedPreferences?.getString("lastLatitudeUser", "20.886953")
            val longitude = sharedPreferences?.getString("lastLongitudeUser", "-76.2593")
            return Point.fromLngLat(
                longitude?.toDouble() ?: -76.2593,
                latitude?.toDouble() ?: 20.886953
            )
        }




        //Last petition for if is closed the app in await
        fun setLastPetition(context: Context, petitionTimeInMills: Long){
            if(sharedPreferences == null) {
                sharedPreferences = context.getSharedPreferences("NovaTaxiAppUser", 0)
            }
            if(sharedEditor == null){
                sharedEditor = sharedPreferences?.edit()
            }
            sharedEditor?.putLong("petitionInMills", petitionTimeInMills)
            sharedEditor?.apply()
        }

        fun getLastPetition(context: Context): Long {
            if(sharedPreferences == null) {
                sharedPreferences = context.getSharedPreferences("NovaTaxiAppUser", 0)
            }
            return sharedPreferences?.getLong("petitionInMills", 0)!!
        }




        //Last petition for if is closed the app without ranking
        fun setIsRatingInAwait(context: Context, isRankingAwait: Boolean){
            if(sharedPreferences == null) {
                sharedPreferences = context.getSharedPreferences("NovaTaxiAppUser", 0)
            }
            if(sharedEditor == null){
                sharedEditor = sharedPreferences?.edit()
            }
            sharedEditor?.putBoolean("isRankingAwait", isRankingAwait)
            sharedEditor?.apply()
        }

        fun getIsRatingInAwait(context: Context): Boolean {
            if(sharedPreferences == null) {
                sharedPreferences = context.getSharedPreferences("NovaTaxiAppUser", 0)
            }
            return sharedPreferences?.getBoolean("isRankingAwait", false)!!
        }




        //Last driver petition
        fun setLastDriver(context: Context, driver: String){
            if(sharedPreferences == null) {
                sharedPreferences = context.getSharedPreferences("NovaTaxiAppUser", 0)
            }
            if(sharedEditor == null){
                sharedEditor = sharedPreferences?.edit()
            }
            sharedEditor?.putString("lastDriver", driver)
            sharedEditor?.apply()
        }

        fun getLastDriver(context: Context): String {
            if(sharedPreferences == null) {
                sharedPreferences = context.getSharedPreferences("NovaTaxiAppUser", 0)
            }
            return sharedPreferences?.getString("lastDriver", "no")!!
        }

    }
}