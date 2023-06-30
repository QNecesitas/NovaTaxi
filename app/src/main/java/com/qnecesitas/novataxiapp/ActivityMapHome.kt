package com.qnecesitas.novataxiapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.utils.internal.toPoint
import com.qnecesitas.novataxiapp.adapters.DriverAdapter
import com.qnecesitas.novataxiapp.auxiliary.ImageTools
import com.qnecesitas.novataxiapp.auxiliary.NetworkTools
import com.qnecesitas.novataxiapp.auxiliary.UserAccountShared
import com.qnecesitas.novataxiapp.databinding.ActivityMapHomeBinding
import com.qnecesitas.novataxiapp.viewmodel.MapHomeViewModel
import com.qnecesitas.novataxiapp.viewmodel.MapHomeViewModelFactory
import com.shashank.sony.fancytoastlib.FancyToast

@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
class ActivityMapHome : AppCompatActivity() {

    //Binding
    private lateinit var binding: ActivityMapHomeBinding

    //Map
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private lateinit var pointAnnotationManagerDriver: PointAnnotationManager
    private lateinit var pointAnnotationManagerUser: PointAnnotationManager


    //ViewModel
    private val viewModel: MapHomeViewModel by viewModels {
        MapHomeViewModelFactory(application)
    }

    //Results launchers
    private lateinit var resultLauncherUbic: ActivityResultLauncher<Intent>
    private lateinit var resultLauncherDest: ActivityResultLauncher<Intent>
    private val permissionCode = 35


    //Navigation
    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onInitialize = this::initNavigation
    )




    /*
    On Create
     */
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //Recycler
        val driverAdapter = DriverAdapter(this@ActivityMapHome)
        binding.rvTaxis.adapter = driverAdapter




        //Results launchers
        resultLauncherUbic =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                locationLocationAccept(result)
            }

        resultLauncherDest =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                locationDestinationAccept(result)
            }




        //Observers
        viewModel.listSmallDriver.observe(this) {
            driverAdapter.submitList(viewModel.listSmallDriver.value)
            updateDriversPositionInMap()
            binding.progressRecycler.visibility = View.GONE
            driverAdapter.notifyDataSetChanged()
        }

        viewModel.stateChargingPrice.observe(this) {
            when(it){
                MapHomeViewModel.StateConstants.LOADING -> binding.progress.visibility = View.VISIBLE
                MapHomeViewModel.StateConstants.SUCCESS -> {
                    binding.progress.visibility = View.GONE
                }
                MapHomeViewModel.StateConstants.ERROR -> {
                    NetworkTools.showAlertDialogNoInternet(this)
                    binding.progress.visibility = View.GONE
                }
            }
        }

        viewModel.routeState.observe(this){
            when(it){
                MapHomeViewModel.StateConstants.LOADING -> {
                    binding.progress.visibility = View.VISIBLE
                }
                MapHomeViewModel.StateConstants.SUCCESS ->{
                    binding.progress.visibility = View.GONE
                    viewModel.route.value?.let { it1 ->
                        drawRouteLine(it1)
                    }
                }
                MapHomeViewModel.StateConstants.ERROR -> {
                    binding.progress.visibility = View.GONE
                    FancyToast.makeText(
                        this@ActivityMapHome ,
                        getString(R.string.error_al_obtener_la_ruta) ,
                        FancyToast.LENGTH_SHORT,FancyToast.ERROR,false
                    ).show()
                }
            }
        }

        viewModel.pointDest.observe(this){

            viewModel.pointUbic.value?.let { pointLocation ->
                viewModel.pointDest.value?.let { pointDest ->
                    viewModel.getRouteToDraw(
                        pointLocation.point,
                        pointDest.point,
                        mapboxNavigation
                    )

                    if(viewModel.listSmallDriver.value?.isEmpty() == true) {
                        binding.clAvailableTaxis.visibility = View.GONE
                        showAlertDialogNoCar()
                    }else{
                        viewModel.getDriverProv()
                        binding.clAvailableTaxis.visibility = View.VISIBLE
                    }

                }
            }
        }
        viewModel.pointUbic.observe(this){

            viewModel.pointUbic.value?.let { pointLocation ->
                viewModel.pointDest.value?.let { pointDest ->
                    viewModel.getRouteToDraw(
                        pointLocation.point,
                        pointDest.point,
                        mapboxNavigation
                    )

                    if(viewModel.listSmallDriver.value?.isEmpty() == true) {
                        binding.clAvailableTaxis.visibility = View.GONE
                        showAlertDialogNoCar()
                    }else{
                        viewModel.getDriverProv()
                        binding.clAvailableTaxis.visibility = View.VISIBLE
                    }

                }
            }
        }
        //TODO Hacer que no se muestre el precio hasta que este calculadp




        //Map
        binding.mapView.getMapboxMap()
            .loadStyleUri("mapbox://styles/ronnynp/cljbn45qs00u201qp84tqauzq/draft")
        val lastPointSelected = UserAccountShared.getLastLocation(this)
        val camera = CameraOptions.Builder()
            .center(Point.fromLngLat(lastPointSelected.longitude(),lastPointSelected.latitude()))
            .zoom(16.0)
            .pitch(50.0)
            .build()
        binding.mapView.scalebar.enabled = false
        binding.mapView.compass.enabled = false
        binding.mapView.getMapboxMap().setCamera(camera)

        binding.extBtnUbicUser.setOnClickListener{ selectUserLocation() }

        binding.extBtnUbicDest.setOnClickListener{ selectUserDest() }



        //Add Map Event
        val annotationApi = binding.mapView.annotations
        pointAnnotationManager = annotationApi.createPointAnnotationManager()
        pointAnnotationManagerDriver = annotationApi.createPointAnnotationManager()
        pointAnnotationManagerUser = annotationApi.createPointAnnotationManager()



        //Listeners
        driverAdapter.setClickDetails(object : DriverAdapter.ITouchDetails{
            override fun onClickDetails(position: Int) {
                val intent = Intent(this@ActivityMapHome,ActivityInfoDriver::class.java)
                intent.putExtra("emailSelected", viewModel.listSmallDriver.value?.get(position)?.email)
                startActivity(intent)
            }
        })

        driverAdapter.setClick(object : DriverAdapter.ITouchAsk{
            override fun onClickAsk(position: Int) {
                showAlertDialogConfirmCar()
            }
        })

        binding.realTimeButton.setOnClickListener{

            val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                viewModel.setIsNecessaryCamera(true)
                getLocationRealtime()

            }else{
                showAlertDialogNotLocationSettings()
            }
        }


        //Start work
        viewModel.setIsNecessaryCamera(true)
        viewModel.startSearchDrivers()
        getLocationRealtime()

    }




    //Get Locations
    private fun selectUserLocation(){
        val intent = Intent(this@ActivityMapHome, ActivityPutMap::class.java)
        resultLauncherUbic.launch(intent)
    }

    private fun selectUserDest(){
        val intent = Intent(this@ActivityMapHome, ActivityPutMap::class.java)
        resultLauncherDest.launch(intent)
    }



    //Send Locations
    private fun locationLocationAccept(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val lat = data?.getDoubleExtra("latitude",0.0)
            val long = data?.getDoubleExtra("longitude",0.0)
            val point = Point.fromLngLat(long!!, lat!!)
            addAnnotationToMap(point, R.drawable.baseline_person_pin_24)
            lat.let { viewModel.setLatitudeClient(it) }
            long.let { viewModel.setLongitudeClient(it) }
        }else{
            FancyToast.makeText(
                this@ActivityMapHome ,
                "Error al encontrar la ubicación" ,
                FancyToast.LENGTH_SHORT,FancyToast.ERROR,false
            ).show()
        }
    }

    private fun locationDestinationAccept(result: ActivityResult){
        if (result.resultCode == Activity.RESULT_OK){
            val data: Intent? = result.data
            val lat = data?.extras?.getDouble("latitude",0.0)
            val long = data?.extras?.getDouble("longitude",0.0)
            val point = Point.fromLngLat(long!!, lat!!)
            addAnnotationToMap(point, R.drawable.marker_map)
        }else{
            FancyToast.makeText(
                this@ActivityMapHome ,
                "Error al encontrar la ubicación" ,
                FancyToast.LENGTH_SHORT,FancyToast.ERROR,false
            ).show()
        }
    }




    //Update all drivers position
    private fun updateDriversPositionInMap(){
        pointAnnotationManagerDriver.deleteAll()
        if(viewModel.listSmallDriver.value !=null) {
            for (it in viewModel.listSmallDriver.value!!) {
                addAnnotationDrivers(
                    Point.fromLngLat(it.longitude, it.latitude),
                    R.drawable.dirver_icon
                )
            }
        }
    }




    //Alert Dialogs
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
                getString(R.string.su_taxi_esta_en_camino),
                FancyToast.LENGTH_LONG,FancyToast.SUCCESS,false).show()
            binding.clAvailableTaxis.visibility = View.GONE

        }

        //create the alert dialog and show it
        builder.create().show()
    }

    private fun showAlertDialogNotLocationSettings() {
        //init alert dialog
        val builder = AlertDialog.Builder(this)
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



    //Methods Maps
    private fun addAnnotationToMap(point: Point, @DrawableRes drawable: Int) {
        ImageTools.bitmapFromDrawableRes(
            this@ActivityMapHome,
            drawable
        )?.let {
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(it)
                .withIconSize(1.5)
            pointAnnotationManager.create(pointAnnotationOptions)

            if(drawable == R.drawable.baseline_person_pin_24){
                if(viewModel.pointUbic.value == null){
                    viewModel.setPointLocation(pointAnnotationManager.annotations.last())
                }else{
                    pointAnnotationManager.delete(viewModel.pointUbic.value!!)
                    viewModel.setPointLocation(pointAnnotationManager.annotations.last())
                }
            }
            if(drawable == R.drawable.marker_map) {
                if (viewModel.pointDest.value == null) {
                    viewModel.setPointDest(pointAnnotationManager.annotations.last())
                } else {
                    pointAnnotationManager.delete(viewModel.pointDest.value!!)
                    viewModel.setPointDest(pointAnnotationManager.annotations.last())
                }
            }
        }
    }

    private fun addAnnotationUser(point: Point, @DrawableRes drawable: Int) {
        pointAnnotationManagerUser.deleteAll()
        ImageTools.bitmapFromDrawableRes(
            this@ActivityMapHome,
            drawable
        )?.let {
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(it)
                .withIconSize(1.5)
            pointAnnotationManagerUser.create(pointAnnotationOptions)
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
                .withIconSize(1.0)
            pointAnnotationManagerDriver.create(pointAnnotationOptions)
        }
    }

    private fun getLocationRealtime() {
        val locationManager =
            this@ActivityMapHome.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                viewModel.setLatitudeClient(location.latitude)
                viewModel.setLongitudeClient(location.longitude)
                val point = Point.fromLngLat(location.longitude, location.latitude)
                addAnnotationUser(point, R.drawable.baseline_directions_walk_24)
                if(viewModel.isNecessaryCamera.value == true) {
                    viewModel.pointGPS.value?.let { it1 -> viewCameraInPoint(it1.point) }
                    viewModel.setIsNecessaryCamera(false)
                }

                UserAccountShared.setLastLocation(
                    this@ActivityMapHome,
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




    //Navigation
    private fun initNavigation() {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .accessToken(getString(R.string.mapbox_access_token))
                .build()
        )
    }

    private fun drawRouteLine(routes: List<NavigationRoute>){
        val routeLineOptions = MapboxRouteLineOptions.Builder(this).build()
        val routeLineApi = MapboxRouteLineApi(routeLineOptions)
        val routeLineView = MapboxRouteLineView(routeLineOptions)


        routeLineApi.setNavigationRoutes(routes) { value ->
            binding.mapView.getMapboxMap().getStyle()
                ?.let { routeLineView.renderRouteDrawData(it, value) }
        }
    }




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




    //Map LifeCycle
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

