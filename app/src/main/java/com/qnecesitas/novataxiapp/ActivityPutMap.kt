package com.qnecesitas.novataxiapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.navigation.utils.internal.toPoint
import com.qnecesitas.novataxiapp.auxiliary.UserAccountShared
import com.qnecesitas.novataxiapp.databinding.ActivityPutMapBinding
import com.qnecesitas.novataxiapp.viewmodel.PutMapViewModel
import com.qnecesitas.novataxiapp.viewmodel.PutMapViewModelFactory
import com.shashank.sony.fancytoastlib.FancyToast


class ActivityPutMap : AppCompatActivity() {
    //Binding
    private lateinit var binding: ActivityPutMapBinding

    //Map
    private lateinit var pointAnnotationManager: PointAnnotationManager

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
        binding.mapView.getMapboxMap()
            //.loadStyleUri("mapbox://styles/ronnynp/cljbmkjqs00gt01qrb2y3bgxj")
            .loadStyleUri(Style.MAPBOX_STREETS)
        val lastPointSelected = UserAccountShared.getLastLocation(this)
        val camera = CameraOptions.Builder()
            .center(Point.fromLngLat(lastPointSelected.longitude(),lastPointSelected.latitude()))
            .zoom(16.0)
            .pitch(50.0)
            .build()
        binding.mapView.getMapboxMap().setCamera(camera)
        binding.mapView.getMapboxMap().addOnMapLongClickListener {

            val point = Point.fromLngLat(it.longitude() , it.latitude())
            addAnnotationToMap(point , R.drawable.marker_map)

            true
        }
        binding.mapView.scalebar.enabled = false
        binding.mapView.compass.enabled = false



        //Listeners
        binding.addLocation.setOnClickListener{
            addLocation()
        }
        binding.realTimeButton.setOnClickListener{

            viewModel.setIsNecessaryCamera(true)
            val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                getLocationRealtime()

            }else{
                showAlertDialogNotLocationSettings()
            }
        }



        viewModel.setIsNecessaryCamera(true)
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
                .withIconSize(0.8)
            val pointAnnotation = pointAnnotationManager.create(pointAnnotationOptions)
            if(drawable == R.drawable.marker_map){
                if(viewModel.pointLocation.value == null){
                    viewModel.setPointLocation(pointAnnotation)
                }else{
                    pointAnnotationManager.delete(viewModel.pointLocation.value!!)
                    viewModel.setPointLocation(pointAnnotation)
                }
            }
            if(drawable == R.drawable.user_icon) {
                if (viewModel.pointGPS.value == null) {
                    viewModel.setPointGPS(pointAnnotation)
                } else {
                    pointAnnotationManager.delete(viewModel.pointGPS.value!!)
                    viewModel.setPointGPS(pointAnnotation)
                }
            }

        }
    }

    //Add Location
    private fun addLocation(){
        val pointSelect = viewModel.pointLocation.value
        val gpsSelect = viewModel.pointGPS.value
        if(pointSelect == null){
            if(gpsSelect == null) {

                FancyToast.makeText(
                    this,
                    getString(R.string.no_ha_añadido_su_posicion),
                    FancyToast.LENGTH_LONG,
                    FancyToast.WARNING,
                    false
                ).show()

            }else{
                showAlertDialogOwnLocation()
            }
        }else{
            val intent = Intent()
            intent.putExtra("longitude", pointSelect.point.longitude())
            intent.putExtra("latitude", pointSelect.point.latitude())
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
                addAnnotationToMap(point, R.drawable.user_icon)
                if(viewModel.isNecessaryCamera.value == true) {
                    viewModel.pointGPS.value?.let { it1 -> viewCameraInPoint(it1.point) }
                    viewModel.setIsNecessaryCamera(false)
                }

                UserAccountShared.setLastLocation(
                    this@ActivityPutMap,
                    location.toPoint()
                )
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

    private fun viewCameraInPoint(point: Point) {
        val camera = CameraOptions.Builder()
            .center(point)
            .zoom(16.5)
            .bearing(50.0)
            .build()
        binding.mapView.getMapboxMap().setCamera(camera)
    }

    private fun showAlertDialogOwnLocation() {
        //init alert dialog
        val builder = android.app.AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setTitle(R.string.Escoger_ubicacion)
        builder.setMessage(R.string.Desea_personal_ubicacion)
        //set listeners for dialog buttons
        builder.setPositiveButton(R.string.Si) { _, _ ->
            val intent = Intent()
            viewModel.pointGPS.value?.point?.let { intent.putExtra("longitude", it.longitude()) }
            viewModel.pointGPS.value?.point?.let { intent.putExtra("latitude", it.latitude()) }
            setResult(RESULT_OK, intent)
            finish()
        }
        builder.setNegativeButton(R.string.No) { dialog, _ ->
            //dialog gone
            dialog.dismiss()
        }
        //create the alert dialog and show it
        builder.create().show()
    }

    private fun showAlertDialogNotLocationSettings() {
        //init alert dialog
        val builder = android.app.AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setTitle(R.string.Ubicacion_desconocida)
        builder.setMessage(R.string.Vaya_a_ajustes)
        //set listeners for dialog buttons
        builder.setPositiveButton(R.string.Aceptar) { dialog, _ ->
            dialog.dismiss()
        }
        //create the alert dialog and show it
        builder.create().show()
    }

}