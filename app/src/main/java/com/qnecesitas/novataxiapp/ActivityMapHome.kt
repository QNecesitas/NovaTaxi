package com.qnecesitas.novataxiapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.qnecesitas.novataxiapp.adapters.DriverAdapter
import com.qnecesitas.novataxiapp.auxiliary.NetworkTools
import com.qnecesitas.novataxiapp.databinding.ActivityMapHomeBinding
import com.qnecesitas.novataxiapp.viewmodel.MapHomeViewModel
import com.qnecesitas.novataxiapp.viewmodel.MapHomeViewModelFactory

class ActivityMapHome : AppCompatActivity() {

    //Binding
    private lateinit var binding: ActivityMapHomeBinding

    //Map
    var mapView: MapView? = null

    //ViewModel
    private val viewModel: MapHomeViewModel by viewModels {
        MapHomeViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }


        //Recycler
        val driverAdapter = DriverAdapter(this@ActivityMapHome)
        binding.rvTaxis.adapter = driverAdapter


        //Observers
        viewModel.listSmallDriver.observe(this) {
            driverAdapter.submitList(viewModel.listSmallDriver.value)
        }

        viewModel.state.observe(this) {
            when(it){
                MapHomeViewModel.StateConstants.LOADING -> binding.progress.visibility = View.VISIBLE
                MapHomeViewModel.StateConstants.SUCCESS -> binding.progress.visibility = View.GONE
                MapHomeViewModel.StateConstants.ERROR -> {
                    NetworkTools.showAlertDialogNoInternet(this)
                    binding.progress.visibility = View.GONE
                }
            }
        }

        //Map
        binding.mapView?.getMapboxMap()?.loadStyleUri(Style.MAPBOX_STREETS)

        binding.extBtnUbicDest.setOnClickListener{ viewModel.getDriverProv() }

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

