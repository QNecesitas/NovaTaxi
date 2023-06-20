package com.qnecesitas.novataxiapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.qnecesitas.novataxiapp.databinding.ActivityInfoDriverBinding
import com.qnecesitas.novataxiapp.databinding.ActivityMainBinding
import com.qnecesitas.novataxiapp.databinding.ActivityMapHomeBinding

class ActivityMapHome : AppCompatActivity() {

    //Binding
    private lateinit var binding: ActivityMapHomeBinding

    //Map
    var mapView: MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        //Map
        binding.mapView?.getMapboxMap()?.loadStyleUri(Style.MAPBOX_STREETS)



    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }


}

