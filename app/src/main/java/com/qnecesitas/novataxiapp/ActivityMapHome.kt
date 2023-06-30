package com.qnecesitas.novataxiapp

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.qnecesitas.novataxiapp.adapters.DriverAdapter
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

    //ViewModel
    private val viewModel: MapHomeViewModel by viewModels {
        MapHomeViewModelFactory()
    }

    //Results launchers
    private lateinit var resultLauncherUbic: ActivityResultLauncher<Intent>
    private lateinit var resultLauncherDest: ActivityResultLauncher<Intent>


    val routeLineOptions = MapboxRouteLineOptions.Builder(this).build()
    val routeLineView = MapboxRouteLineView(routeLineOptions)
    val routeLineApi = MapboxRouteLineApi(routeLineOptions)

    val annotationApi = binding.mapView.annotations

    val onPositionChangedListener = OnIndicatorPositionChangedListener { point ->
        val result = routeLineApi.updateTraveledRouteLine(point)
        routeLineView.renderRouteLineUpdate(binding.mapView.getMapboxMap().getStyle()!!, result)
    }
    //MapBox
    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onInitialize = this::initNavigation
    )

    //Arrow
    val routeArrow = MapboxRouteArrowApi()
    val routeArrowOptions = RouteArrowOptions.Builder(this).build()
    val routeArrowView = MapboxRouteArrowView(routeArrowOptions)

    /*
    On Create
     */
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
                locationUbicAccept(result)
            }
        resultLauncherDest =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                locationDestAccept(result)
            }



        //Observers
        viewModel.listSmallDriver.observe(this) {
            if(viewModel.listSmallDriver.value?.isNotEmpty() == true){
                driverAdapter.submitList(viewModel.listSmallDriver.value)
                binding.clAvailableTaxis.visibility = View.VISIBLE
                viewModel.fetchARoute(this,mapboxNavigation)
                drawRute()
            }else{
                driverAdapter.submitList(viewModel.listSmallDriver.value)
                binding.clAvailableTaxis.visibility = View.GONE
                showAlertDialogNoCar()
            }
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

        val routeProgressObserver = RouteProgressObserver { routeProgress ->
            routeLineApi.updateWithRouteProgress(routeProgress) { result ->
                routeLineView.renderRouteLineUpdate(binding.mapView.getMapboxMap()
                    .getStyle()!!, result)
            }
        }

        mapView?.location?.addOnIndicatorPositionChangedListener(onPositionChangedListener)

        //Map
        binding.mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)

        binding.extBtnUbicUser.setOnClickListener{ getUserUbic() }

        binding.extBtnUbicDest.setOnClickListener{ getUserDest() }

        //Add Map Event
        pointAnnotationManager = annotationApi.createPointAnnotationManager()

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

    }

    //init
    private fun initNavigation() {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .accessToken(getString(R.string.mapbox_access_token))
                .build()
        )
    }


    //Get Locations
    fun getUserUbic(){
        val intent = Intent(this@ActivityMapHome, ActivityPutMap::class.java)
        resultLauncherUbic.launch(intent)
    }

    fun getUserDest(){
        val intent = Intent(this@ActivityMapHome, ActivityPutMap::class.java)
        resultLauncherDest.launch(intent)
    }




    //Send Locations
    fun locationUbicAccept(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val lat = data?.getDoubleExtra("latitude",0.0)
            val long = data?.getDoubleExtra("longitude",0.0)
            val point = Point.fromLngLat(long!!, lat!!)
            addAnnotationToMap(point, R.drawable.baseline_person_pin_24)
            lat.let { viewModel.setLatitudeClient(it) }
            long.let { viewModel.setLongitudeClient(it) }
            if (viewModel.latitudeDestiny.value != null && viewModel.longitudeDestiny.value != null) {

                viewModel.getDriverProv(
                    viewModel.latitudeClient.value!!,
                    viewModel.longitudeClient.value!!
                )
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

    fun locationDestAccept(result: ActivityResult){
        if (result.resultCode == Activity.RESULT_OK){
            val data: Intent? = result.data
            val lat = data?.extras?.getDouble("latitude",0.0)
            val long = data?.extras?.getDouble("longitude",0.0)
            lat?.let { viewModel.setLatitudeDestiny(it) }
            long?.let { viewModel.setLongitudeDestiny(it) }
            val point = Point.fromLngLat(long!!, lat!!)
            addAnnotationToMap(point, R.drawable.marker_map)
            if (viewModel.latitudeClient.value != null && viewModel.longitudeClient.value != null) {
                viewModel.getDriverProv(
                    viewModel.latitudeClient.value!!,
                    viewModel.longitudeClient.value!!
                )
            }
        }else{
            FancyToast.makeText(
                this@ActivityMapHome ,
                "Error al encontrar la ubicación" ,
                FancyToast.LENGTH_SHORT,FancyToast.ERROR,false
            ).show()
        }
    }




    //Alert Dialogs
    fun showAlertDialogNoCar(){
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




    //Methods Maps
    private fun addAnnotationToMap(point: Point, @DrawableRes drawable: Int) {
        bitmapFromDrawableRes(
            this@ActivityMapHome,
            drawable
        )?.let {
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(it)
                .withIconSize(2.0)
            val pointAnnotation = pointAnnotationManager.create(pointAnnotationOptions)

            if(drawable == R.drawable.baseline_person_pin_24){
                if(viewModel.pointUbic.value == null){
                    viewModel.setPointUbic(pointAnnotation)
                }else{
                    pointAnnotationManager.delete(viewModel.pointUbic.value!!)
                    viewModel.setPointUbic(pointAnnotation)
                }
            }
            if(drawable == R.drawable.marker_map) {
                if (viewModel.pointDest.value == null) {
                    viewModel.setPointDest(pointAnnotation)
                } else {
                    pointAnnotationManager.delete(viewModel.pointDest.value!!)
                    viewModel.setPointDest(pointAnnotation)
                }
            }
        }
    }

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    private fun drawRute(){
        val routerCallback = object : NavigationRouterCallback {

            val routeLineOptions = MapboxRouteLineOptions
                .Builder(this@ActivityMapHome)
                .withVanishingRouteLineEnabled(true)
                .withRouteLineBelowLayerId("road-label")
                .build()


            val routeProgressObserver = object : RouteProgressObserver {
                override fun onRouteProgressChanged(routeProgress: RouteProgress) {
                    val updatedManeuverArrow = routeArrow.addUpcomingManeuverArrow(routeProgress)
                    routeArrowView.renderManeuverUpdate(binding.mapView.getMapboxMap().getStyle()!!
                        , updatedManeuverArrow)
                }
            }

            override fun onRoutesReady(routes: List<NavigationRoute>, routerOrigin: RouterOrigin) {
                // note: the first route in the list is considered the primary route
                routeLineApi.setNavigationRoutes(routes) { value ->
                    routeLineView.renderRouteDrawData(binding.mapView.getMapboxMap().getStyle()!!
                        , value)
                }

            }

            override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
            }

            override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
            }

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




    //Override Methods
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

