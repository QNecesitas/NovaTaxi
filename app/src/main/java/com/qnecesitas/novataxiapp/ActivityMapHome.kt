package com.qnecesitas.novataxiapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.utils.internal.toPoint
import com.qnecesitas.novataxiapp.adapters.TypeTaxiAdapter
import com.qnecesitas.novataxiapp.auxiliary.ImageTools
import com.qnecesitas.novataxiapp.auxiliary.NetworkTools
import com.qnecesitas.novataxiapp.auxiliary.UserAccountShared
import com.qnecesitas.novataxiapp.databinding.ActivityMapHomeBinding
import com.qnecesitas.novataxiapp.databinding.LiRateDriverBinding
import com.qnecesitas.novataxiapp.model.Driver
import com.qnecesitas.novataxiapp.model.Vehicle
import com.qnecesitas.novataxiapp.viewmodel.LoginViewModel
import com.qnecesitas.novataxiapp.viewmodel.MapHomeViewModel
import com.qnecesitas.novataxiapp.viewmodel.MapHomeViewModelFactory
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
class ActivityMapHome : AppCompatActivity() {

    //Binding
    private lateinit var binding: ActivityMapHomeBinding

    //Map elements
    private lateinit var pointAnnotationManagerPositions: PointAnnotationManager
    private lateinit var pointAnnotationManagerDrivers: PointAnnotationManager
    private lateinit var pointAnnotationManagerTrip: PointAnnotationManager

    //Permissions
    private val permissionCode = 34

    //ViewModel
    private val viewModel: MapHomeViewModel by viewModels {
        MapHomeViewModelFactory()
    }

    //Results launchers
    private lateinit var resultLauncherLocation: ActivityResultLauncher<Intent>
    private lateinit var resultLauncherDest: ActivityResultLauncher<Intent>

    //MapBox
    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onInitialize = this::initNavigation
    )

    //Route
    private lateinit var routeLineOptions: MapboxRouteLineOptions
    private lateinit var routeLineView: MapboxRouteLineView
    private lateinit var routeLineApi: MapboxRouteLineApi
    private lateinit var routeArrow: MapboxRouteArrowApi
    private lateinit var routeArrowOptions: RouteArrowOptions
    private lateinit var routeArrowView: MapboxRouteArrowView


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
        pointAnnotationManagerTrip = annotationApi.createPointAnnotationManager()




        //Route
        routeLineOptions = MapboxRouteLineOptions.Builder(this).build()
        routeLineView = MapboxRouteLineView(routeLineOptions)
        routeLineApi = MapboxRouteLineApi(routeLineOptions)
        routeArrow = MapboxRouteArrowApi()
        routeArrowOptions = RouteArrowOptions.Builder(this).build()
        routeArrowView = MapboxRouteArrowView(routeArrowOptions)


        //Results launchers
        resultLauncherLocation =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                locationOriginAccept(result)
            }
        resultLauncherDest =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                locationDestAccept(result)
            }




        //Recycler
        val adapterType = TypeTaxiAdapter(this)
        adapterType.setClickDetails(object: TypeTaxiAdapter.ITouchDetails{
            override fun onClickDetails(vehicle: Vehicle) {
                showAlertDialogCarDetails(vehicle.details)
            }
        })
        adapterType.setClick(object: TypeTaxiAdapter.ITouchAsk{
            override fun onClickAsk(vehicle: Vehicle) {
                 showAlertDialogConfirmCar(vehicle)
            }
        })
        binding.rvTaxis.adapter = adapterType
        binding.rvTaxis.setHasFixedSize(true)



        //Observers
        viewModel.listDrivers.observe(this) {
            updateDriversPositionInMap()
        }

        viewModel.statePrices.observe(this){
            when(it){
                MapHomeViewModel.StateConstants.LOADING ->
                    binding.progressRecycler.visibility = View.VISIBLE
                MapHomeViewModel.StateConstants.SUCCESS ->{
                    binding.progressRecycler.visibility = View.GONE
                }
                MapHomeViewModel.StateConstants.ERROR -> {
                    NetworkTools.showAlertDialogNoInternet(this)
                    binding.progressRecycler.visibility = View.GONE
                }
            }
        }

        viewModel.listVehicle.observe(this){
            adapterType.submitList(it)
        }

        viewModel.stateAddTrip.observe(this){
            when(it){
                MapHomeViewModel.StateConstants.LOADING -> {
                    binding.progressRecycler.visibility = View.VISIBLE
                    binding.rvTaxis.visibility = View.GONE
                }
                MapHomeViewModel.StateConstants.SUCCESS ->{
                    showAlertLLAwaitSelect(TimeUnit.MINUTES.toMillis(10))
                }
                MapHomeViewModel.StateConstants.ERROR -> {
                    NetworkTools.showAlertDialogNoInternet(this)
                    binding.progressRecycler.visibility = View.GONE
                }
            }
        }

        viewModel.stateCancelTrip.observe(this){
            when(it){
                MapHomeViewModel.StateConstants.LOADING -> {
                    binding.progressCancelAwait.visibility = View.VISIBLE
                }
                MapHomeViewModel.StateConstants.SUCCESS ->{
                    UserAccountShared.setLastPetition(this, 0)
                    binding.progressCancelAwait.visibility = View.GONE
                    binding.llAwaitCarSelect.visibility = View.GONE
                    binding.extBtnUbicUser.visibility = View.VISIBLE
                    binding.extBtnUbicDest.visibility = View.VISIBLE
                }
                MapHomeViewModel.StateConstants.ERROR -> {
                    NetworkTools.showAlertDialogNoInternet(this)
                    binding.progressCancelAwait.visibility = View.GONE
                }
            }
        }

        viewModel.stateRate.observe(this){
            when(it){
                MapHomeViewModel.StateConstants.LOADING -> {

                }
                MapHomeViewModel.StateConstants.SUCCESS ->{
                    FancyToast.makeText(
                        this@ActivityMapHome ,
                        getString(R.string.valorado_con_exito) ,
                        FancyToast.LENGTH_SHORT,FancyToast.SUCCESS,false
                    ).show()
                }
                MapHomeViewModel.StateConstants.ERROR -> {
                    FancyToast.makeText(
                        this@ActivityMapHome ,
                        getString(R.string.error_al_valorar) ,
                        FancyToast.LENGTH_SHORT,FancyToast.ERROR,false
                    ).show()
                }
            }
        }

        viewModel.tripState.observe(this){
            if(it == "Aceptado"){
                val intent = Intent(this, ActivityNavigation::class.java)
                startActivity(intent)
            }
        }

        viewModel.stateVersion.observe(this){
            when(it){
                LoginViewModel.StateConstants.LOADING -> binding.progress.visibility = View.VISIBLE
                LoginViewModel.StateConstants.SUCCESS -> {
                    getUserDestination()
                }
                LoginViewModel.StateConstants.ERROR ->{
                    NetworkTools.showAlertDialogNoInternet(this)
                    binding.progress.visibility = View.GONE
                }
            }
        }

        viewModel.versionResponse.observe(this){
            showAlertDialogNotVersion(it)
        }


        //Listeners
        binding.realTimeButton.setOnClickListener{

            //UserAccountShared.setIsRatingInAwait(this,true)
            //UserAccountShared.setLastDriver(this,"chofer")

            val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                viewModel.setIsNecessaryCamera(true)
                getLocationRealtime()

            }else{
                showAlertDialogNotLocationSettings()
            }
        }

        binding.extBtnUbicUser.setOnClickListener{ getUserOrigin() }

        binding.extBtnUbicDest.setOnClickListener{ viewModel.getAppVersion() }

        binding.settingsButton.setOnClickListener {
            val intent = Intent(this, ActivityUserSettings::class.java)
            startActivity(intent)
        }



        //Start Search
        viewModel.setIsNecessaryCamera(true)
        getLocationRealtime()
        viewModel.startMainCoroutine(this)
        checkInitialSharedInformation()
    }


    //init
    private fun initNavigation() {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .accessToken(getString(R.string.mapbox_access_token))
                .build()
        )
    }

    private fun checkInitialSharedInformation(){
        if(UserAccountShared.getIsRatingInAwait(this)){
            rateTheDriver()
            UserAccountShared.setIsRatingInAwait(this,false)
        }

        val lastTimeInMills = UserAccountShared.getLastPetition(this)
        val actualTimeInMills = Calendar.getInstance().timeInMillis

        if(lastTimeInMills > 0) {
            if ((actualTimeInMills - lastTimeInMills) < 600000) {
                showAlertLLAwaitSelect(600000  - (actualTimeInMills - lastTimeInMills))
            }
        }

    }

    private fun rateTheDriver(){
        liRateDriver()
    }

    private fun showAlertDialogNotVersion(url: String) {
        //init alert dialog
        val builder = android.app.AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle(R.string.Version_desactualizada)
        builder.setMessage(getString(R.string.version_incorrecta, url))
        //set listeners for dialog buttons
        builder.setPositiveButton(
            R.string.Aceptar
        ) { dialog, _ ->
            dialog.dismiss()
        }

        builder.setNegativeButton(getString(R.string.descargar)){
            dialog, _ ->
            dialog.dismiss()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        //create the alert dialog and show it
        builder.create().show()
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
                viewModel.setLatitudeGPS(location.latitude)
                viewModel.setLongitudeGPS(location.longitude)

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

    private fun getUserOrigin(){
        val intent = Intent(this@ActivityMapHome, ActivityPutMap::class.java)
        resultLauncherLocation.launch(intent)
    }

    private fun getUserDestination(){
        val intent = Intent(this@ActivityMapHome, ActivityPutMap::class.java)
        resultLauncherDest.launch(intent)
    }





    //Send Locations
    private fun locationOriginAccept(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val lat = data?.getDoubleExtra("latitude",0.0)
            val long = data?.getDoubleExtra("longitude",0.0)
            val point = Point.fromLngLat(long!!, lat!!)
            addAnnotationsTripToMap(point, R.drawable.start_route)
            lat.let { viewModel.setLatitudeClient(it) }
            long.let { viewModel.setLongitudeClient(it) }
            if (viewModel.latitudeDestiny.value != null && viewModel.longitudeDestiny.value != null) {
                fetchARoute()
            }
        }else{
            FancyToast.makeText(
                this@ActivityMapHome ,
                getString(R.string.error_al_encontrar_la_ubicaci_n) ,
                FancyToast.LENGTH_SHORT,FancyToast.ERROR,false
            ).show()
        }
    }

    private fun locationDestAccept(result: ActivityResult){
        if (result.resultCode == Activity.RESULT_OK){
            val data: Intent? = result.data
            val lat = data?.extras?.getDouble("latitude",0.0)
            val long = data?.extras?.getDouble("longitude",0.0)
            lat?.let { viewModel.setLatitudeDestiny(it) }
            long?.let { viewModel.setLongitudeDestiny(it) }
            val point = Point.fromLngLat(long!!, lat!!)
            addAnnotationsTripToMap(point, R.drawable.end_route)
            if (viewModel.latitudeOrigin.value != null && viewModel.longitudeOrigin.value != null) {
                fetchARoute()
            }
        }else{
            FancyToast.makeText(
                this@ActivityMapHome ,
                getString(R.string.error_al_encontrar_la_ubicaci_n) ,
                FancyToast.LENGTH_SHORT,FancyToast.ERROR,false
            ).show()
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

    private fun showAlertDialogCarDetails(details: String) {
        //init alert dialog
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setTitle(getString(R.string.detalles))
        builder.setMessage(details)
        //set listeners for dialog buttons
        builder.setPositiveButton(R.string.Aceptar) { dialog, _ ->
            dialog.dismiss()
        }
        //create the alert dialog and show it
        builder.create().show()
    }

    fun showAlertDialogConfirmCar(vehicle: Vehicle){
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
            viewModel.addTrip(
                this@ActivityMapHome,
                vehicle.price.toDouble(),
                "no",
                vehicle.type
            )
        }

        //create the alert dialog and show it
        builder.create().show()
    }



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
                        getDriverIcon(it)
                    )
                }
            }
        }
    }

    private fun getDriverIcon(driver: Driver): Int{
        return when(driver.typeCar){
            "Auto simple" -> R.drawable.dirver_icon_simple
            "Auto de confort" -> R.drawable.dirver_icon_confort
            "Auto familiar" -> R.drawable.dirver_icon_familiar
            "Triciclo" -> R.drawable.dirver_icon_triciclo
            "Motor" -> R.drawable.dirver_icon_motor
            else -> R.drawable.dirver_icon_simple

        }
    }

    private fun addAnnotationsTripToMap(point: Point, @DrawableRes drawable: Int) {
        viewModel.bitmapFromDrawableRes(
            this@ActivityMapHome,
            drawable
        )?.let {
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(it)
                .withIconSize(0.8)
            pointAnnotationManagerTrip.create(pointAnnotationOptions)
            if(drawable == R.drawable.start_route){
                if(viewModel.pointOrigin.value == null){
                    viewModel.setPointOrigin(pointAnnotationManagerTrip.annotations.last())
                }else{
                    pointAnnotationManagerTrip.delete(viewModel.pointOrigin.value!!)
                    viewModel.setPointOrigin(pointAnnotationManagerTrip.annotations.last())
                }
            }
            if(drawable ==  R.drawable.end_route) {
                if (viewModel.pointDest.value == null) {
                    viewModel.setPointDest(pointAnnotationManagerTrip.annotations.last())
                } else {
                    pointAnnotationManagerTrip.delete(viewModel.pointDest.value!!)
                    viewModel.setPointDest(pointAnnotationManagerTrip.annotations.last())
                }
            }

        }
    }





    //Route
    private fun fetchARoute() {
        //Declarations
        binding.clAvailableTaxis.visibility = View.VISIBLE
        binding.progressRecycler.visibility = View.VISIBLE
        val originPoint = Point.fromLngLat(
            viewModel.longitudeOrigin.value!!,
            viewModel.latitudeOrigin.value!!
        )

        val destPoint = Point.fromLngLat(
            viewModel.longitudeDestiny.value!!,
            viewModel.latitudeDestiny.value!!
        )

        val originLocation = Location("test").apply {
            longitude = viewModel.longitudeOrigin.value!!
            latitude =  viewModel.latitudeOrigin.value!!
            bearing = 10f
        }


        val routeOptions = RouteOptions.builder()
            .applyDefaultNavigationOptions()
            .applyLanguageAndVoiceUnitOptions(this@ActivityMapHome)
            .coordinatesList(listOf(originPoint,destPoint))
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
                    binding.clAvailableTaxis.visibility = View.GONE
                    binding.progressRecycler.visibility = View.GONE
                    FancyToast.makeText(
                        this@ActivityMapHome ,
                        getString(R.string.error_al_encontrar_la_ruta) ,
                        FancyToast.LENGTH_SHORT,FancyToast.ERROR,false
                    ).show()
                }

                override fun onRoutesReady(
                    routes: List<NavigationRoute>,
                    routerOrigin: RouterOrigin
                ) {
                    viewCameraInPoint(destPoint)
                    drawRouteLine(routes)
                    viewModel.getPrices(viewModel.getRouteDistance(routes))
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




    //Await car
    private fun showAlertLLAwaitSelect(timeInMills: Long){
        lifecycleScope.launch {
            UserAccountShared.setLastPetition(this@ActivityMapHome,Calendar.getInstance().timeInMillis)
            binding.llAwaitCarSelect.visibility = View.VISIBLE
            binding.extBtnUbicUser.visibility = View.GONE
            binding.extBtnUbicDest.visibility = View.GONE
            binding.clAvailableTaxis.visibility = View.GONE
            binding.llCancelAwait.setOnClickListener{
                viewModel.cancelTaxiAwait(this@ActivityMapHome)
            }
            delay(timeInMills)
            UserAccountShared.setLastPetition(this@ActivityMapHome,0)
            binding.llAwaitCarSelect.visibility = View.GONE
            binding.extBtnUbicUser.visibility = View.VISIBLE
            binding.extBtnUbicDest.visibility = View.VISIBLE
        }
    }

    private fun liRateDriver(){
        val inflater = LayoutInflater.from(binding.root.context)
        val liBinding = LiRateDriverBinding.inflate(inflater)
        val builder = androidx.appcompat.app.AlertDialog.Builder(binding.root.context)
        builder.setView(liBinding.root)
        val alertDialog = builder.create()


        liBinding.btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        liBinding.btnRate.setOnClickListener {
            alertDialog.dismiss()
            viewModel.rateTaxi(
                this,
                liBinding.rating.rating.toInt()
            )
        }



        //Finish
        builder.setCancelable(true)
        builder.setTitle(R.string.Recuperar_contrasena)
        alertDialog.window!!.setGravity(Gravity.CENTER)
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
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

