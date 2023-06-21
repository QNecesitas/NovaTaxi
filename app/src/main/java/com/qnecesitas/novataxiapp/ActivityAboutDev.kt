package com.qnecesitas.novataxiapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.qnecesitas.novataxiapp.databinding.ActivityAboutDevBinding

class ActivityAboutDev : AppCompatActivity() {

    private lateinit var binding: ActivityAboutDevBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutDevBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //Toolbar
        setSupportActionBar(binding.AADToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        binding.AADToolbar.setNavigationOnClickListener { finish() }
    }
}