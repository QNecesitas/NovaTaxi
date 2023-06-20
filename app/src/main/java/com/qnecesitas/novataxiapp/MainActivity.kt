package com.qnecesitas.novataxiapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val TIEMPO_DE_ESPERA: Long = 2000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(/* layoutResID = */ R.layout.activity_main)
        val escondedor = window.decorView
        escondedor.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)

        //Handler

        //Handler
        @SuppressLint("HandlerLeak") val handler: Handler = object : Handler() {
            override fun handleMessage(message: Message) {
                if (message.arg1 == 1) {
                    val intent = Intent(this@MainActivity , ActivityMapHome::class.java)
                    startActivity(intent)
                }
            }
        }


        //Thread


        //Thread
        val thread = Thread {
            try {
                Thread.sleep(TIEMPO_DE_ESPERA)
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