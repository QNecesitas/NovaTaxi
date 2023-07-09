package com.qnecesitas.novataxiapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.utils.internal.toPoint
import com.qnecesitas.novataxiapp.adapters.DriverAdapter
import com.qnecesitas.novataxiapp.auxiliary.ImageTools
import com.qnecesitas.novataxiapp.auxiliary.UserAccountShared
import com.qnecesitas.novataxiapp.databinding.ActivityMapHomeBinding
import com.qnecesitas.novataxiapp.viewmodel.MapHomeViewModel
import com.qnecesitas.novataxiapp.viewmodel.MapHomeViewModelFactory

@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
class ActivityMapHome : AppCompatActivity() {

    //Binding
    private lateinit var binding: ActivityMapHomeBinding

    //Map elements
    private lateinit var pointAnnotationManagerPositions: PointAnnotationManager
    private lateinit var pointAnnotationManagerDrivers: PointAnnotationManager

    //Permissions
    private val permissionCode = 34

    //ViewModel
    private val viewModel: MapHomeViewModel by viewModels {
        MapHomeViewModelFactory()
    }

    //Results launchers
    private lateinit var resultLauncherUbic: ActivityResultLauncher<Intent>
    private lateinit var resultLauncherDest: ActivityResultLauncher<Intent>



    //MapBox
    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onInitialize = this::initNavigation
    )


    /*
    On Create
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)




        //Map
        binding.mapView.getMapboxMap()
            .loadStyleUri("mapbox://styles/ronnynp/cljbmkjqs00gt01qrb2y3bgxj")
        val lastPointSelected = UserAccountShared.getLastLocation(this)
        val camera = CameraOptions.Builder()
            .center(Point.fromLngLat(lastPointSelected.longitude(),lastPointSelected.latitude()))
            .zoom(16.5)
            .pitch(70.0)
            .build()
        binding.mapView.getMapboxMap().setCamera(camera)
        binding.mapView.scalebar.enabled = false
        binding.mapView.compass.enabled = false
        val annotationApi = binding.mapView.annotations
        pointAnnotationManagerPositions = annotationApi.createPointAnnotationManager()
        pointAnnotationManagerDrivers = annotationApi.createPointAnnotationManager()




        //Recycler
        val driverAdapter = viewModel.pointUbic.value?.let {
            viewModel.pointDest.value?.let { it1 ->
                DriverAdapter(
                    this@ActivityMapHome,
                    mapboxNavigation,
                    it.point,
                    it1.point
                )
            }
        }
        binding.rvTaxis.adapter = driverAdapter




        /*--
        //Results launchers
        resultLauncherUbic =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                locationUbicAccept(result)
            }
        resultLauncherDest =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                locationDestAccept(result)
            }
         ----*/



        //Observers
        viewModel.listDrivers.observe(this) {
            updateDriversPositionInMap()
        }
        /*----
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
         ----*/


        //Listeners
        binding.realTimeButton.setOnClickListener{

            val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                viewModel.setIsNecessaryCamera(true)
                getLocationRealtime()

            }else{
                showAlertDialogNotLocationSettings()
            }
        }
        /*----
        driverAdapter?.setClickDetails(object : DriverAdapter.ITouchDetails{
            override fun onClickDetails(position: Int) {
                val intent = Intent(this@ActivityMapHome,ActivityInfoDriver::class.java)
                intent.putExtra("emailSelected", viewModel.listSmallDriver.value?.get(position)?.email)
                startActivity(intent)
            }
        })

        driverAdapter?.setClick(object : DriverAdapter.ITouchAsk{
            override fun onClickAsk(driver: Driver, price: Int) {
                showAlertDialogConfirmCar()
            }
        })

        binding.extBtnUbicUser.setOnClickListener{ getUserOrigin() }

        binding.extBtnUbicDest.setOnClickListener{ getUserDestination() }
         ----*/



        //Start Search
        viewModel.setIsNecessaryCamera(true)
        getLocationRealtime()
        viewModel.startMainCoroutine()
    }


    //init
    private fun initNavigation() {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .accessToken(getString(R.string.mapbox_access_token))
                .build()
        )
    }




    //Location
    private fun getLocationRealtime() {
        val locationManager =
            this@ActivityMapHome.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {

                //Draw an icon with the position
                val point = Point.fromLngLat(location.longitude, location.latitude)
                addAnnotationGPSToMap(point, R.drawable.user_icon)

                //Put camera in GPS position if is necessary
                if(viewModel.isNecessaryCamera.value == true) {
                    viewCameraInPoint(location.toPoint())
                    viewModel.setIsNecessaryCamera(false)
                }

                //Last location is saved for when open the map, it open here
                UserAccountShared.setLastLocation(
                    this@ActivityMapHome,
                    Point.fromLngLat(location.longitude, location.latitude)
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




    /*----
    //Get Locations
    private fun getUserOrigin(){
        val intent = Intent(this@ActivityMapHome, ActivityPutMap::class.java)
        resultLauncherUbic.launch(intent)
    }

    private fun getUserDestination(){
        val intent = Intent(this@ActivityMapHome, ActivityPutMap::class.java)
        resultLauncherDest.launch(intent)
    }
     ----*/




    /*---
    //Send Locations
    private fun locationUbicAccept(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val lat = data?.getDoubleExtra("latitude",0.0)
            val long = data?.getDoubleExtra("longitude",0.0)
            val point = Point.fromLngLat(long!!, lat!!)
            addAnnotationToMap(point, R.drawable.baseline_person_pin_24)
            lat.let { viewModel.setLatitudeClient(it) }
            long.let { viewModel.setLongitudeClient(it) }
            if (viewModel.latitudeDestiny.value != null && viewModel.longitudeDestiny.value != null) {
                viewModel.getDriverProv()
            }
        }else{
            FancyToast.makeText(
                this@ActivityMapHome ,
                "Error al encontrar la ubicación" ,
                FancyToast.LENGTH_SHORT,FancyToast.ERROR,false
            ).show()
        }
    }
        //binding.extBtnUbicDest.setOnClickListener{ viewModel.getDriverProv() }

    private fun locationDestAccept(result: ActivityResult){
        if (result.resultCode == Activity.RESULT_OK){
            val data: Intent? = result.data
            val lat = data?.extras?.getDouble("latitude",0.0)
            val long = data?.extras?.getDouble("longitude",0.0)
            lat?.let { viewModel.setLatitudeDestiny(it) }
            long?.let { viewModel.setLongitudeDestiny(it) }
            val point = Point.fromLngLat(long!!, lat!!)
            addAnnotationToMap(point, R.drawable.marker_map)
            if (viewModel.latitudeClient.value != null && viewModel.longitudeClient.value != null) {
                viewModel.getDriverProv()
            }
        }else{
            FancyToast.makeText(
                this@ActivityMapHome ,
                "Error al encontrar la ubicación" ,
                FancyToast.LENGTH_SHORT,FancyToast.ERROR,false
            ).show()
        }
    }
     ----*/




    //Alert Dialogs
    private fun showAlertDialogNotLocationSettings() {
        //init alert dialog
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
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
    /*----s
    private fun showAlertDialogNoCar(){
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle(this.getString(R.string.NoCarroEncontrado))
        builder.setMessage(R.string.NoCarro)
        builder.setPositiveButton(
            R.string.Aceptar
        ) { dialog, _ -> dialog.dismiss() }

        //create the alert dialog and show it
        builder.create().show()
    }

    fun showAlertDialogConfirmCar(){
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle(this.getString(R.string.ConfirmTaxi))
        builder.setMessage(R.string.SeguroPedirTaxi)
        builder.setNegativeButton(
            R.string.Cancelar
        ){dialog,_->
            dialog.dismiss()}
        builder.setPositiveButton(
            R.string.Aceptar
        ) {
                dialog,_->
            dialog.dismiss()
            FancyToast.makeText(this@ActivityMapHome,
                "Su taxi esta en camino, por favor espere",
                FancyToast.LENGTH_LONG,FancyToast.SUCCESS,false).show()
            binding.clAvailableTaxis.visibility = View.GONE

        }

        //create the alert dialog and show it
        builder.create().show()
    }
     ----*/



    //Methods Maps
    private fun addAnnotationGPSToMap(point: Point, @DrawableRes drawable: Int) {
        pointAnnotationManagerPositions.deleteAll()
        viewModel.bitmapFromDrawableRes(
            this@ActivityMapHome,
            drawable
        )?.let {
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(it)
                .withIconSize(0.8)
            pointAnnotationManagerPositions.create(pointAnnotationOptions)

        }
    }

    private fun addAnnotationDrivers(point: Point, @DrawableRes drawable: Int) {
        ImageTools.bitmapFromDrawableRes(
            this@ActivityMapHome,
            drawable
        )?.let {
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(it)
                .withIconSize(0.8)
            pointAnnotationManagerDrivers.create(pointAnnotationOptions)
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

    private fun updateDriversPositionInMap() {
        pointAnnotationManagerDrivers.deleteAll()
        if (viewModel.listDrivers.value != null) {
            for (it in viewModel.listDrivers.value!!) {
                if (it.longitude != 0.0 && it.latitude != 0.0) {
                    addAnnotationDrivers(
                        Point.fromLngLat(it.longitude, it.latitude),
                        R.drawable.dirver_icon
                    )
                }
            }
        }
    }


    /*----
    private fun addAnnotationGPSToMap(point: Point, @DrawableRes drawable: Int) {
        viewModel.bitmapFromDrawableRes(
            this@ActivityMapHome,
            drawable
        )?.let {
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(it)
                .withIconSize(0.8)
            pointAnnotationManagerPositions.create(pointAnnotationOptions)
            if(drawable == R.drawable.marker_map){
                if(viewModel.pointLocation.value == null){
                    viewModel.setPointLocation(pointAnnotationManagerPositions.annotations.last())
                }else{
                    pointAnnotationManagerPositions.delete(viewModel.pointLocation.value!!)
                    viewModel.setPointLocation(pointAnnotationManagerPositions.annotations.last())
                }
            }
            if(drawable ==  R.drawable.dirver_icon_mine) {
                if (viewModel.pointGPS.value == null) {
                    viewModel.setPointGPS(pointAnnotationManagerPositions.annotations.last())
                } else {
                    pointAnnotationManagerPositions.delete(viewModel.pointGPS.value!!)
                    viewModel.setPointGPS(pointAnnotationManagerPositions.annotations.last())
                }
            }

        }
    }
     ----*/






    //Exit Apk
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        showAlertDialogExit()
    }

    private fun showAlertDialogExit() {
        //init alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setTitle(R.string.salir)
        builder.setMessage(R.string.seguro_desea_salir)
        //set listeners for dialog buttons
        builder.setPositiveButton(R.string.Si) { _: DialogInterface?, _: Int ->
            //finish the activity
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        builder.setNegativeButton(R.string.no) { dialog: DialogInterface, _: Int ->
            //dialog gone
            dialog.dismiss()
        }
        //create the alert dialog and show it
        builder.create().show()
    }




    //Override Methods
    @SuppressLint("Lifecycle")
    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    @SuppressLint("Lifecycle")
    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    @SuppressLint("Lifecycle")
    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    @SuppressLint("Lifecycle")
    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }


}

