package com.qnecesitas.novataxiapp

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.qnecesitas.novataxiapp.adapters.DriverAdapter
import com.qnecesitas.novataxiapp.auxiliary.ImageTools
import com.qnecesitas.novataxiapp.auxiliary.NetworkTools
import com.qnecesitas.novataxiapp.databinding.ActivityMapHomeBinding
import com.qnecesitas.novataxiapp.viewmodel.MapHomeViewModel
import com.qnecesitas.novataxiapp.viewmodel.MapHomeViewModelFactory
import com.shashank.sony.fancytoastlib.FancyToast

@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
class ActivityMapHome : AppCompatActivity() {

    //Binding
    private lateinit var binding: ActivityMapHomeBinding

    //Map
    var mapView: MapView? = null
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private lateinit var pointAnnotationManagerDriver: PointAnnotationManager


    //ViewModel
    private val viewModel: MapHomeViewModel by viewModels {
        MapHomeViewModelFactory(application)
    }

    //Results launchers
    private lateinit var resultLauncherUbic: ActivityResultLauncher<Intent>
    private lateinit var resultLauncherDest: ActivityResultLauncher<Intent>


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
            viewModel.pointUbic.value?.let { it1 ->
                viewModel.pointDest.value?.let { it2 ->
                    viewModel.getRouteToDraw(
                        it1.point,
                        it2.point,
                        mapboxNavigation
                    )
                }
            }
            if(viewModel.listSmallDriver.value?.isNotEmpty() == true){
                driverAdapter.submitList(viewModel.listSmallDriver.value)
                updateDriversPositionInMap()
                binding.clAvailableTaxis.visibility = View.VISIBLE
            }else{
                driverAdapter.submitList(viewModel.listSmallDriver.value)
                driverAdapter.notifyDataSetChanged()
                binding.clAvailableTaxis.visibility = View.GONE
                showAlertDialogNoCar()
            }
        }

        viewModel.state.observe(this) {
            when(it){
                MapHomeViewModel.StateConstants.LOADING -> binding.progress.visibility = View.VISIBLE
                MapHomeViewModel.StateConstants.SUCCESS -> {
                    viewModel.updatePricesInList(mapboxNavigation)
                }
                MapHomeViewModel.StateConstants.ERROR -> {
                    NetworkTools.showAlertDialogNoInternet(this)
                    binding.progress.visibility = View.GONE
                }
            }
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

        viewModel.outputWorkList.observe(this){
            if(it.isNullOrEmpty()){
                return@observe
            }

            val workInfo = it[0]
            if(workInfo.state.isFinished){
                viewModel.latitudeClient.value?.let { it1 ->
                    viewModel.longitudeClient.value?.let { it2 ->
                    viewModel.getDriverProv(it1, it2)
                } }
            }
        }




        //Map
        binding.mapView.getMapboxMap().loadStyleUri("mapbox://styles/ronnynp/cljbn45qs00u201qp84tqauzq/draft")
        val camera = CameraOptions.Builder()
            .center(Point.fromLngLat(-76.2593,20.886953))
            .zoom(16.0)
            .pitch(50.0)
            .build()
        binding.mapView.getMapboxMap().setCamera(camera)

        binding.extBtnUbicUser.setOnClickListener{ selectUserLocation() }

        binding.extBtnUbicDest.setOnClickListener{ selectUserDest() }



        //Add Map Event
        val annotationApi = binding.mapView.annotations
        pointAnnotationManager = annotationApi.createPointAnnotationManager()
        pointAnnotationManagerDriver = annotationApi.createPointAnnotationManager()



        //Recycler Listeners
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



        //Start work
        viewModel.startSearchWork()
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
            lat?.let { viewModel.setLatitudeDestiny(it) }
            long?.let { viewModel.setLongitudeDestiny(it) }
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
        pointAnnotationManager.deleteAll()
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



    //Methods Maps
    private fun addAnnotationToMap(point: Point, @DrawableRes drawable: Int) {
        ImageTools.bitmapFromDrawableRes(
            this@ActivityMapHome,
            drawable
        )?.let {
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(it)
                .withIconSize(2.0)
            pointAnnotationManager.create(pointAnnotationOptions)

            if(drawable == R.drawable.baseline_person_pin_24){
                if(viewModel.pointUbic.value == null){
                    viewModel.setPointUbic(pointAnnotationManager.annotations.last())
                }else{
                    pointAnnotationManager.delete(viewModel.pointUbic.value!!)
                    viewModel.setPointUbic(pointAnnotationManager.annotations.last())
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
        val camera = CameraOptions.Builder()
            .center(point)
            .zoom(16.5)
            .bearing(50.0)
            .build()
        binding.mapView.getMapboxMap().setCamera(camera)
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
            val pointAnnotation = pointAnnotationManager.create(pointAnnotationOptions)
        }
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
        builder.setPositiveButton(R.string.Si) { _: DialogInterface?, which: Int ->
            //finish the activity
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        builder.setNegativeButton(R.string.no) { dialog: DialogInterface, which: Int ->
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
        mapView?.onStart()
    }

    @SuppressLint("Lifecycle")
    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    @SuppressLint("Lifecycle")
    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    @SuppressLint("Lifecycle")
    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

}

