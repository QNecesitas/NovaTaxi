package com.qnecesitas.novataxiapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.qnecesitas.novataxiapp.auxiliary.RoutesTools
import com.qnecesitas.novataxiapp.auxiliary.UserAccountShared
import com.qnecesitas.novataxiapp.model.Trip
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val delayTime: Long = 2000




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




        //Full Screen code and default night mode
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)




        //Getting if already logged
        val logged = UserAccountShared.getUserEmail(this) != null




        //Change activity in a time
        lifecycleScope.launch {
            delay(delayTime)
            if(logged){
                val intent = Intent(this@MainActivity, ActivityMapHome::class.java)
                /*
                RoutesTools.navigationTrip = Trip(
                    "chofer",
                    "usuario",
                    200.0,
                    4.300,
                    "10/10/2023",
                    21.359560163262543,
                    -77.93554676386799,
                    21.391481886830675,
                    -77.96554676386799,
                    "Aceptado",
                    0,
                    0.0,
                    "Auto familiar"
                )
                RoutesTools.latitudeDriver = 21.361461886830675
                RoutesTools.longitudeDriver = -77.935246763868
                 */
                startActivity(intent)
            }else{
                val intent = Intent(this@MainActivity, ActivityLogin::class.java)
                startActivity(intent)
            }
        }

    }
}