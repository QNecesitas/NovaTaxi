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
    private val _listDrivers = MutableLiveData<MutableList<Driver>>()
    val listDrivers: LiveData<MutableList<Driver>> get() = _listDrivers

    //Progress state
    enum class StateConstants { LOADING, SUCCESS, ERROR }

    private val _state = MutableLiveData<StateConstants>()
    val state: LiveData<StateConstants> get() = _state

    //Network Data Source
    private var driverDataSourceNetwork: DriverDataSourceNetwork = DriverDataSourceNetwork()

    //LatitudeClient
    private val _latitudeOrigin = MutableLiveData<Double>()
    val latitudeOrigin: LiveData<Double> get() = _latitudeOrigin

    //LongitudeClient
    private val _longitudeOrigin = MutableLiveData<Double>()
    val longitudeOrigin: LiveData<Double> get() = _longitudeOrigin

    //LatitudeDestiny
    private val _latitudeDestiny = MutableLiveData<Double>()
    val latitudeDestiny: LiveData<Double> get() = _latitudeDestiny

    //LongitudeDestiny
    private val _longitudeDestiny = MutableLiveData<Double>()
    val longitudeDestiny: LiveData<Double> get() = _longitudeDestiny

    //Points
    private val _pointOrigin = MutableLiveData<PointAnnotation>()
    val pointOrigin: LiveData<PointAnnotation> get() = _pointOrigin

    private val _pointDest = MutableLiveData<PointAnnotation>()
    val pointDest: LiveData<PointAnnotation> get() = _pointDest

    //LatitudeClient
    private val _latitudeGPS = MutableLiveData<Double>()
    val latitudeGPS: LiveData<Double> get() = _latitudeGPS

    //LongitudeClient
    private val _longitudeGPS = MutableLiveData<Double>()
    val longitudeGPS: LiveData<Double> get() = _longitudeGPS



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




    //Obtaining the data
    private fun getDriversAll() {
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
                    if(response.body() != null) {
                        filterDriver(response.body()!!.toMutableList())
                    }
                } else {
                    _state.value = StateConstants.ERROR
                }
            }

            override fun onFailure(call: Call<List<Driver>>, t: Throwable) {
                _state.value = StateConstants.ERROR
            }
        })
    }

    private fun filterDriver(alDriver: MutableList<Driver>){
        if(latitudeGPS.value !=null && longitudeGPS.value!= null) {
            val alResult = alDriver.filter {
                it.maxDist > calculateDist(
                    latitudeGPS.value!!,
                    longitudeGPS.value!!,
                    it.latitude,
                    it.longitude
                )
            }.toMutableList()
            _listDrivers.value = alResult
        }
    }


    //Calculate Distance
    private fun calculateDist(
        latUser: Double,
        longUser: Double,
        latCar: Double,
        longCar: Double
    ): Double {
        val radiusEarth = 6371 // Radio de la Tierra en km
        val lat1Rad = Math.toRadians(latUser)
        val lon1Rad = Math.toRadians(longUser)
        val lat2Rad = Math.toRadians(latCar)
        val lon2Rad = Math.toRadians(longCar)
        val distanceLat = lat2Rad - lat1Rad
        val distanceLon = lon2Rad - lon1Rad
        val a = sin(distanceLat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(distanceLon / 2).pow(2)
        val c = 2 * asin(sqrt(a))
        return radiusEarth * c
    }



    //Setters
    fun setLatitudeClient(lat: Double){
        _latitudeOrigin.value = lat
    }

    fun setLongitudeClient(long: Double){
        _longitudeOrigin.value = long
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

    fun setPointOrigin(point: PointAnnotation){
        _pointOrigin.value = point
    }

    fun setPointDest(point: PointAnnotation){
        _pointDest.value = point
    }

    fun setIsNecessaryCamera(boolean: Boolean){
        _isNecessaryCamera.value = boolean
    }

    fun setLatitudeGPS(latitude: Double){
        _latitudeGPS.value = latitude
    }

    fun setLongitudeGPS(longitude: Double){
        _longitudeGPS.value = longitude
    }



    //Route
    fun fetchARoute(context: Context,mapboxNavigation: MapboxNavigation) {

        //Declarations
        val originPoint = Point.fromLngLat(
            longitudeOrigin.value!!,
            latitudeOrigin.value!!
        )

        val destPoint = Point.fromLngLat(
            longitudeDestiny.value!!,
            latitudeDestiny.value!!
        )

        val originLocation = Location("test").apply {
            longitude = longitudeOrigin.value!!
            latitude =  latitudeOrigin.value!!
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
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val json = routes.map {
                        gson.toJson(
                            JsonParser.parseString(it.directionsRoute.toJson())
                        )
                    }

                }
            }
        )

    }




    //Corutina
    fun startMainCoroutine(){
        viewModelScope.launch {
            while (true){
                getDriversAll()
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