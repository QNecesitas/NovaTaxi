package com.qnecesitas.novataxiapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.qnecesitas.novataxiapp.databinding.ActivityInfoDriverBinding
import com.qnecesitas.novataxiapp.databinding.ActivityLoginBinding

class Activity_InfoDriver : AppCompatActivity() {

    private lateinit var binding: ActivityInfoDriverBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoDriverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }


    }
}