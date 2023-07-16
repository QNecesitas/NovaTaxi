package com.qnecesitas.novataxiapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
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
import com.qnecesitas.novataxiapp.auxiliary.UserAccountShared
import com.qnecesitas.novataxiapp.databinding.ActivityNavigationBinding
import com.shashank.sony.fancytoastlib.FancyToast

class ActivityNavigation : AppCompatActivity() {


    //Binding
    private lateinit var binding: ActivityNavigationBinding


    //Navigation
    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onInitialize = this::initNavigation
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
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


        //Start routing
        //startRouting()
        showAlertDialogExit()

    }


    private fun fetchARoute(
        latitudeOrigin: Double,
        longitudeOrigin: Double,
        latitudeDestiny: Double,
        longitudeDestiny: Double,
        latitudeDriver: Double,
        longitudeDriver: Double
    ) {
        //Declarations
        val originPoint = Point.fromLngLat(
            longitudeOrigin,
            latitudeOrigin
        )

        val destPoint = Point.fromLngLat(
            longitudeDestiny,
            latitudeDestiny
        )

        val driverPoint = Point.fromLngLat(
            longitudeDriver,
            latitudeDriver
        )

        val originLocation = Location("test").apply {
            longitude = longitudeOrigin
            latitude = latitudeOrigin
            bearing = 10f
        }

        val routeOptions = RouteOptions.builder()
            .applyDefaultNavigationOptions()
            .applyLanguageAndVoiceUnitOptions(this@ActivityNavigation)
            .coordinatesList(listOf(driverPoint, originPoint, destPoint))
            .alternatives(false)
            .bearingsList(
                listOf(
                    Bearing.builder()
                        .angle(originLocation.bearing.toDouble())
                        .degrees(45.0)
                        .build(),
                    null,
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
                        FancyToast.LENGTH_SHORT, FancyToast.ERROR,false
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



    //ShowAlertDialogs
    private fun showAlertDialogExit() {
        //init alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setTitle(R.string.Danada)
        builder.setMessage(getString(R.string.esta_pantalla_parece_contener_un_error))
        //set listeners for dialog buttons
        builder.setPositiveButton(R.string.salir) { _: DialogInterface?, _: Int ->
            //finish the activity
            finish()
        }
        //create the alert dialog and show it
        builder.create().show()
    }



    //Map LifeCycle
    @SuppressLint("Lifecycle")
    override fun onStart() {
        super.onStart()
        binding.mapView?.onStart()
    }

    @SuppressLint("Lifecycle")
    override fun onPause() {
        super.onPause()
    }

    @SuppressLint("Lifecycle")
    override fun onStop() {
        super.onStop()
        binding.mapView?.onStop()
    }

    @SuppressLint("Lifecycle")
    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView?.onLowMemory()
    }

    @SuppressLint("Lifecycle")
    override fun onDestroy() {
        super.onDestroy()
        binding.mapView?.onDestroy()
    }

    private fun initNavigation() {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .accessToken(getString(R.string.mapbox_access_token))
                .build()
        )
    }

}