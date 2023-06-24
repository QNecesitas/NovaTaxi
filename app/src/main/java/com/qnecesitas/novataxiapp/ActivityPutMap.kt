package com.qnecesitas.novataxiapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.get
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import com.qnecesitas.novataxiapp.databinding.ActivityPutMapBinding
import com.qnecesitas.novataxiapp.viewmodel.PutMapViewModel
import com.qnecesitas.novataxiapp.viewmodel.PutMapViewModelFactory
import com.shashank.sony.fancytoastlib.FancyToast


class ActivityPutMap : AppCompatActivity() {
    //Binding
    private lateinit var binding: ActivityPutMapBinding

    //Map
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private var pointSelect: Point? = null

    //ViewModel
    private val viewModel: PutMapViewModel by viewModels {
        PutMapViewModelFactory()
    }

    //Permissions
    private val permissionCode = 34


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPutMapBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //Toolbar
        setSupportActionBar(binding.mapToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        binding.mapToolbar.setNavigationOnClickListener { finish() }

        //Add Map Event
        val annotationApi = binding.mapView.annotations
        pointAnnotationManager = annotationApi.createPointAnnotationManager()
        binding.mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
        binding.mapView.getMapboxMap().addOnMapLongClickListener {

            val point = Point.fromLngLat(it.longitude() , it.latitude())
            addAnnotationToMap(point , R.drawable.marker_map)

            true
        }
        binding.addLocation.setOnClickListener{
            addLocation()
        }
        binding.realTimeButton.setOnClickListener{
            getLocationRealtime()
        }



        val permissionCheck:Int =ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)

        getLocationRealtime()

    }


    private fun addAnnotationToMap(point: Point, @DrawableRes drawable: Int) {
        viewModel.bitmapFromDrawableRes(
            this@ActivityPutMap,
            drawable
        )?.let {
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(it)
                .withIconSize(2.0)
            pointAnnotationManager.create(pointAnnotationOptions)
            if(drawable == R.drawable.marker_map){
                if(viewModel.pointLocation.value == null){
                    viewModel.setPointLocation(pointAnnotationManager.annotations.last())
                }else{
                    pointAnnotationManager.delete(viewModel.pointLocation.value!!)
                    viewModel.setPointLocation(pointAnnotationManager.annotations.last())
                }
            }
            if(drawable == R.drawable.baseline_blur_circular_24) {
                if (viewModel.pointGPS.value == null) {
                    viewModel.setPointGPS(pointAnnotationManager.annotations.last())
                } else {
                    pointAnnotationManager.delete(viewModel.pointGPS.value!!)
                    viewModel.setPointGPS(pointAnnotationManager.annotations.last())
                }
            }

        }
    }

    //Add Location
    private fun addLocation(){

        if(pointSelect == null){
            FancyToast.makeText(this,getString(R.string.no_ha_añadido_su_posicion),FancyToast.LENGTH_LONG,FancyToast.WARNING,false).show()

        }else{
            val intent = Intent()
            intent.putExtra("longitude", pointSelect!!.longitude())
            intent.putExtra("latitude", pointSelect!!.latitude())
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun getLocationRealtime() {
        val locationManager =
            this@ActivityPutMap.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val point = Point.fromLngLat(location.longitude, location.latitude)
                addAnnotationToMap(point, R.drawable.baseline_blur_circular_24)


            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            }

            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        val permissionCheck: Int =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0,
                0f,
                locationListener
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                permissionCode
            )
        }
    }

}