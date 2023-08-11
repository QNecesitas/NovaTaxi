package com.qnecesitas.novataxiapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.UiModeManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.qnecesitas.novataxiapp.auxiliary.ImageTools
import com.qnecesitas.novataxiapp.auxiliary.NetworkTools
import com.qnecesitas.novataxiapp.auxiliary.RoutesTools
import com.qnecesitas.novataxiapp.auxiliary.UserAccountShared
import com.qnecesitas.novataxiapp.databinding.ActivityNavigationBinding
import com.qnecesitas.novataxiapp.model.Driver
import com.qnecesitas.novataxiapp.model.Trip
import com.qnecesitas.novataxiapp.viewmodel.NavigationViewModel
import com.qnecesitas.novataxiapp.viewmodel.NavigationViewModelFactory
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ActivityNavigation : AppCompatActivity() {

    //Binding
    private lateinit var binding: ActivityNavigationBinding

    //Map elements
    private lateinit var pointAnnotationManagerPositions: PointAnnotationManager
    private lateinit var pointAnnotationManagerDrivers: PointAnnotationManager
    private lateinit var pointAnnotationManagerTrip: PointAnnotationManager

    //Permissions
    private val permissionCode = 34

    //ViewModel
    private val viewModel: NavigationViewModel by viewModels {
        NavigationViewModelFactory()
    }

    //MapBox
    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onInitialize = this::initNavigation
    )

    //Notification
    private val CHANNEL_ID: String = "NovaTaxi"
    private val CHANNEL_NAME = "NovaTaxi"
    lateinit var notificationManager : NotificationManager



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)



        //Map
        val uiModeManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        val styleUri = if (uiModeManager.nightMode == UiModeManager.MODE_NIGHT_YES) {
            "mapbox://styles/ronnynp/cljbmkjqs00gt01qrb2y3bgxj"
        } else {
            "mapbox://styles/ronnynp/clkdk8gcm008f01qwff3m06dy"
        }
        binding.mapView.getMapboxMap()
            .loadStyleUri(styleUri)
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
        pointAnnotationManagerTrip = annotationApi.createPointAnnotationManager()




        //Notification
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager




        //Listeners
        binding.realTimeButton.setOnClickListener {
            getLocationRealtime()
        }




        //Observers
        viewModel.driver.observe(this){ driver->
            viewModel.setLatitudeDriver(driver.latitude)
            viewModel.setLongitudeDriver(driver.longitude)
            addAnnotationDrivers(
                Point.fromLngLat(driver.longitude, driver.latitude),
                getDriverIcon(driver)
            )
            RoutesTools.navigationTrip?.let { fetchARoute(it) }
        }

        viewModel.actualTrip.observe(this){
            when(it.state){
                "Espera por cliente"->{
                    showAwaitOptions(true)
                    notificationManager.notify(9,displayNotification())
                }
                "En viaje"->{
                    showAwaitOptions(false)
                    showAlertDialogStartedTrip()
                }
                "Finalizado"->{
                    showAlertDialogFinish()
                }
            }
        }

        viewModel.actualPrices.observe(this){
            if(it != null){
                if(it.isNotEmpty()){
                    val prices = it[0]
                    showAlertDialogPrices(prices.delayTime, prices.priceDelay)
                }else{
                    NetworkTools.showAlertDialogNoInternet(this)
                }
            }else{
                NetworkTools.showAlertDialogNoInternet(this)
            }
        }

        //Start Search
        UserAccountShared.setIsRatingInAwait(this,true)
        RoutesTools.navigationTrip?.let { UserAccountShared.setLastDriver(this, it.fk_driver) }
        addRoutePoints()//Start-end points
        RoutesTools.navigationTrip?.let {
            viewCameraInPoint(
                Point.fromLngLat(it.longOri,it.latOri)
            )
        }//Camera
        getLocationRealtime()//User location
        startMainRoutine()//Main thread

    }

    //init
    private fun initNavigation() {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .accessToken(getString(R.string.mapbox_access_token))
                .build()
        )
        RoutesTools.navigationTrip?.let { fetchARoute(it) }
    }

    private fun addRoutePoints(){
        val pointOrigin = RoutesTools.navigationTrip?.let {
            Point.fromLngLat(it.longOri, it.latOri)
        }
        if (pointOrigin != null) {
            addAnnotationsTripToMap(pointOrigin, R.drawable.start_route)
        }
        val pointDestination = RoutesTools.navigationTrip?.let {
            Point.fromLngLat(it.longDest, it.latDest)
        }
        if (pointDestination != null) {
            addAnnotationsTripToMap(pointDestination, R.drawable.end_route)
        }
    }

    private fun startMainRoutine(){
        lifecycleScope.launch {
            while (true){
                if(NetworkTools.isOnline(this@ActivityNavigation, true)) {
                    RoutesTools.navigationTrip?.let { viewModel.getDriverPosition(it.fk_driver)}
                    RoutesTools.navigationTrip?.let { fetchARoute(it) }
                    UserAccountShared.getUserEmail(this@ActivityNavigation)
                        ?.let { viewModel.fetchStateInTrip(it) }
                }
                delay(TimeUnit.SECONDS.toMillis(30))
            }
        }
    }




    //Location
    private fun getLocationRealtime() {
        var isNecessaryCamera = true

        val locationManager =
            this@ActivityNavigation.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            val locationListener: LocationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    //Draw an icon with the position
                    val point = Point.fromLngLat(location.longitude, location.latitude)
                    addAnnotationGPSToMap(point, R.drawable.user_icon)
                    if(isNecessaryCamera){
                        viewCameraInPoint(point)
                        isNecessaryCamera = false
                    }

                    //Last location is saved for when open the map, it open here
                    UserAccountShared.setLastLocation(
                        this@ActivityNavigation,
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

        }else{
            showAlertDialogNotLocationSettings()
        }
    }




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


    //Methods Maps
    private fun addAnnotationGPSToMap(point: Point, @DrawableRes drawable: Int) {
        pointAnnotationManagerPositions.deleteAll()
        viewModel.bitmapFromDrawableRes(
            this@ActivityNavigation,
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
        pointAnnotationManagerDrivers.deleteAll()
        ImageTools.bitmapFromDrawableRes(
            this@ActivityNavigation,
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

    private fun getDriverIcon(driver: Driver): Int{
        return when(driver.typeCar){
            "Auto básico" -> R.drawable.dirver_icon_simple
            "Auto de confort" -> R.drawable.dirver_icon_confort
            "Auto familiar" -> R.drawable.dirver_icon_familiar
            "Triciclo" -> R.drawable.dirver_icon_triciclo
            "Motor" -> R.drawable.dirver_icon_motor
            "Bicitaxi" -> R.drawable.dirver_icon_bicitaxi
            else -> R.drawable.dirver_icon_simple

        }
    }

    private fun addAnnotationsTripToMap(point: Point, @DrawableRes drawable: Int) {
        viewModel.bitmapFromDrawableRes(
            this@ActivityNavigation,
            drawable
        )?.let {
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(it)
                .withIconSize(0.8)
            pointAnnotationManagerTrip.create(pointAnnotationOptions)
        }
    }






    //Route
    private fun fetchARoute(navigationTrip: Trip){

        val originPoint = Point.fromLngLat(
            navigationTrip.longOri,
            navigationTrip.latOri,
        )

        val destPoint = Point.fromLngLat(
            navigationTrip.longDest,
            navigationTrip.latDest,
        )

        val driverPoint = viewModel.longitudeDriver.value?.let {
            viewModel.latitudeDriver.value?.let { it1 ->
                Point.fromLngLat(
                    it,
                    it1
                )
            }
        }


        if (driverPoint != null) {
            if(viewModel.actualTrip.value != null) {
                if (viewModel.actualTrip.value!!.state == "Espera por cliente"||
                    viewModel.actualTrip.value!!.state == "En viaje") {
                    setRouteOptions1Step( destPoint, driverPoint)
                }else{
                    setRouteOptions2Steps( originPoint, destPoint, driverPoint)
                }
            }
        }
    }

    private fun setRouteOptions2Steps(originP: Point, destP: Point, driverP: Point) {
        val originLocation = Location("test").apply {
            longitude = driverP.longitude()
            latitude =  driverP.latitude()
            bearing = 10f
        }


        val routeOptions = RouteOptions.builder()
            .applyDefaultNavigationOptions()
            .applyLanguageAndVoiceUnitOptions(this@ActivityNavigation)
            .coordinatesList(listOf(driverP, originP, destP))
            .alternatives(false)
            .bearingsList(
                listOf(
                    Bearing.builder()
                        .angle(originLocation.bearing.toDouble())
                        .degrees(45.0)
                        .build(),
                    null,
                    null
                )
            )
            .build()

        mapboxNavigation.requestRoutes(
            routeOptions,
            object : NavigationRouterCallback {
                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
                }

                override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
                    FancyToast.makeText(
                        this@ActivityNavigation ,
                        getString(R.string.error_al_encontrar_la_ruta) ,
                        FancyToast.LENGTH_SHORT,FancyToast.ERROR,false
                    ).show()
                    Log.e("TEST",reasons.toString())
                }

                override fun onRoutesReady(
                    routes: List<NavigationRoute>,
                    routerOrigin: RouterOrigin
                ) {
                    drawRouteLine(routes)
                }
            }
        )

    }

    private fun setRouteOptions1Step(destP: Point, driverP: Point) {
        val originLocation = Location("test").apply {
            longitude = driverP.longitude()
            latitude =  driverP.latitude()
            bearing = 10f
        }


        val routeOptions = RouteOptions.builder()
            .applyDefaultNavigationOptions()
            .applyLanguageAndVoiceUnitOptions(this@ActivityNavigation)
            .coordinatesList(listOf(driverP, destP))
            .alternatives(false)
            .bearingsList(
                listOf(
                    Bearing.builder()
                        .angle(originLocation.bearing.toDouble())
                        .degrees(45.0)
                        .build(),
                    null
                )
            )
            .build()

        mapboxNavigation.requestRoutes(
            routeOptions,
            object : NavigationRouterCallback {
                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
                }

                override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
                    FancyToast.makeText(
                        this@ActivityNavigation ,
                        getString(R.string.error_al_encontrar_la_ruta) ,
                        FancyToast.LENGTH_SHORT,FancyToast.ERROR,false
                    ).show()
                }

                override fun onRoutesReady(
                    routes: List<NavigationRoute>,
                    routerOrigin: RouterOrigin
                ) {
                    drawRouteLine(routes)
                }
            }
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




    //Route await
    private fun showAwaitOptions(open: Boolean){
        if(open) {
            binding.clAwait.visibility = View.VISIBLE
            binding.llBtnAwait.setOnClickListener {
                viewModel.fetchPrices()
            }
        }else{
            binding.clAwait.visibility = View.GONE
        }
    }

    private fun showAlertDialogPrices(time: Int, price: Int) {
        val message = "Cada $time minutos de espera se suman $price CUP al precio total"
        //init alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setTitle(R.string.impuestos)
        builder.setMessage(message)
        //set listeners for dialog buttons
        builder.setPositiveButton(R.string.Aceptar) { dialog: DialogInterface?, _: Int ->

            dialog?.dismiss()

        }
        //create the alert dialog and show it
        builder.create().show()
    }

    private fun showAlertDialogStartedTrip() {
        //init alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setMessage(R.string.su_viaje_ha_comenzado)
        //set listeners for dialog buttons
        builder.setPositiveButton(R.string.Aceptar) { dialog: DialogInterface?, _: Int ->

            dialog?.dismiss()

        }
        //create the alert dialog and show it
        builder.create().show()
    }

    private fun displayNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            channel.description = getString(R.string.channel_decr)
            channel.enableLights(true)
            channel.lightColor = Color.RED
            notificationManager.createNotificationChannel(channel)
        }

        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setContentTitle(getString(R.string.auto_llegado))
                .setContentText(getString(R.string.vehiculo_espera))
                .setSmallIcon(R.drawable.baseline_drive_eta_24)
                .setAutoCancel(false)

        return builder.build()

    }





    //Finishing
    private fun showAlertDialogFinish() {
        if(viewModel.actualTrip.value != null) {
            val totalPrice =
                viewModel.actualTrip.value!!.travelPrice + viewModel.actualTrip.value!!.priceAwait
            val message =
                "El precio total del viaje fue de $totalPrice CUP, le hemos enviado un correo con los detalles del viaje"
            //init alert dialog
            val builder = AlertDialog.Builder(this)
            builder.setCancelable(false)
            builder.setTitle(R.string.viaje_finalizado)
            builder.setMessage(message)
            //set listeners for dialog buttons
            builder.setPositiveButton(R.string.Aceptar) { _: DialogInterface?, _: Int ->
                //finish the activity
                val intent = Intent(this, ActivityMapHome::class.java)
                startActivity(intent   )
            }
            //create the alert dialog and show it
            builder.create().show()
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