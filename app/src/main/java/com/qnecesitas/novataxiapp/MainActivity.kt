package com.qnecesitas.novataxiapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private val delayTime: Long = 2000


    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(
                WindowInsets.Type.statusBars()
            )
        }else{
            val decorator = window.decorView
            decorator.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }


        //Handler
        @SuppressLint("HandlerLeak") val handler: Handler = object : Handler() {
            override fun handleMessage(message: Message) {
                if (message.arg1 == 1) {
                    val intent = Intent(this@MainActivity, ActivityLogin::class.java)
                    startActivity(intent)
                }
            }
        }


        //Thread
        val thread = Thread {
            try {
                Thread.sleep(delayTime)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val message = Message.obtain()
            message.arg1 = 1
            handler.sendMessage(message)
        }
        thread.start()

    }
}