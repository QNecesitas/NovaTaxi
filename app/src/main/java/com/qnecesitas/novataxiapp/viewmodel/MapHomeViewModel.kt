package com.qnecesitas.novataxiapp.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigation
import com.qnecesitas.novataxiapp.auxiliary.Constants
import com.qnecesitas.novataxiapp.model.Driver
import com.qnecesitas.novataxiapp.network.DriverDataSourceNetwork
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit
import kotlin.math.*


class MapHomeViewModel: ViewModel() {

    //List user
    private val _listSmallDriver = MutableLiveData<MutableList<Driver>>()
    val listDrivers: LiveData<MutableList<Driver>> get() = _listSmallDriver

    //Progress state
    enum class StateConstants { LOADING, SUCCESS, ERROR }

    private val _state = MutableLiveData<StateConstants>()
    val state: LiveData<StateConstants> get() = _state

    //Network Data Source
    private var driverDataSourceNetwork: DriverDataSourceNetwork = DriverDataSourceNetwork()

    //LatitudeClient
    private val _latitudeClient = MutableLiveData<Double>()
    val latitudeClient: LiveData<Double> get() = _latitudeClient

    //LongitudeClient
    private val _longitudeClient = MutableLiveData<Double>()
    val longitudeClient: LiveData<Double> get() = _longitudeClient

    //LatitudeDestiny
    private val _latitudeDestiny = MutableLiveData<Double>()
    val latitudeDestiny: LiveData<Double> get() = _latitudeDestiny

    //LongitudeDestiny
    private val _longitudeDestiny = MutableLiveData<Double>()
    val longitudeDestiny: LiveData<Double> get() = _longitudeDestiny

    //Points
    private val _pointUbic = MutableLiveData<PointAnnotation>()
    val pointUbic: LiveData<PointAnnotation> get() = _pointUbic

    private val _pointDest = MutableLiveData<PointAnnotation>()
    val pointDest: LiveData<PointAnnotation> get() = _pointDest




    //Variable to setCamera
    private val _isNecessaryCamera = MutableLiveData<Boolean>()
    val isNecessaryCamera: LiveData<Boolean> get() = _isNecessaryCamera




    //Points
    private val _pointLocation = MutableLiveData<PointAnnotation>()
    val pointLocation: LiveData<PointAnnotation> get() = _pointLocation

    private val _pointGPS = MutableLiveData<PointAnnotation>()
    val pointGPS: LiveData<PointAnnotation> get() = _pointGPS




    //Converting image asset
    fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
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




    //Bring InfoDriver
    fun getDriverProv() {
        _state.value = StateConstants.LOADING

        //Call
        val call = driverDataSourceNetwork.getDriver(
            Constants.PHP_TOKEN,
        )
        getResponseInfoDriverProv(call)
    }

    //Get the response about the Driver info
    private fun getResponseInfoDriverProv(call: Call<List<Driver>>) {
        call.enqueue(object : Callback<List<Driver>> {
            override fun onResponse(
                call: Call<List<Driver>>,
                response: Response<List<Driver>>
            ) {
                if (response.isSuccessful) {
                    _state.value = StateConstants.SUCCESS
                    _listSmallDriver.value = response.body()?.toMutableList()
                } else {
                    _state.value = StateConstants.ERROR
                }
            }

            override fun onFailure(call: Call<List<Driver>>, t: Throwable) {
                _state.value = StateConstants.ERROR
            }
        })
    }




    //Setters
    fun setLatitudeClient(lat: Double){
        _latitudeClient.value = lat
    }

    fun setLongitudeClient(long: Double){
        _longitudeClient.value = long
    }

    fun setLatitudeDestiny(lat: Double){
        _latitudeDestiny.value = lat
    }

    fun setLongitudeDestiny(long: Double){
        _longitudeDestiny.value = long
    }

    fun setPointLocation(point: PointAnnotation){
        _pointLocation.value = point
    }

    fun setPointGPS(point: PointAnnotation){
        _pointGPS.value = point
    }

    fun setPointUbic(point: PointAnnotation){
        _pointUbic.value = point
    }

    fun setPointDest(point: PointAnnotation){
        _pointDest.value = point
    }

    fun setIsNecessaryCamera(boolean: Boolean){
        _isNecessaryCamera.value = boolean
    }




    //Route
    fun fetchARoute(context: Context,mapboxNavigation: MapboxNavigation) {

        //Declarations
        val originPoint = Point.fromLngLat(
            longitudeClient.value!!,
            latitudeClient.value!!
        )

        val destPoint = Point.fromLngLat(
            longitudeDestiny.value!!,
            latitudeDestiny.value!!
        )

        val originLocation = Location("test").apply {
            longitude = longitudeClient.value!!
            latitude =  latitudeClient.value!!
            bearing = 10f
        }

        val routeOptions = RouteOptions.builder()
            .applyDefaultNavigationOptions()
            .applyLanguageAndVoiceUnitOptions(context)
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
// This particular callback is executed if you invoke
// mapboxNavigation.cancelRouteRequest()
                }

                override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {

                }

                override fun onRoutesReady(
                    routes: List<NavigationRoute>,
                    routerOrigin: RouterOrigin
                ) {
// GSON instance used only to print the response prettily
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val json = routes.map {
                        gson.toJson(
                            JsonParser.parseString(it.directionsRoute.toJson())
                        )
                    }
                    Log.e("tusae", json.toString())

                }
            }
        )

    }




    //Corutina
    fun startMainCoroutine(){
        viewModelScope.launch {
            while (true){
                getDriverProv()
                delay(TimeUnit.SECONDS.toMillis(20))
            }
        }
    }

}


class MapHomeViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapHomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapHomeViewModel() as T
        }
        throw IllegalArgumentException("Unknown viewModel class")
    }
}