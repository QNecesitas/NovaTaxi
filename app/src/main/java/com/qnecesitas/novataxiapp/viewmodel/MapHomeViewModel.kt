package com.qnecesitas.novataxiapp.viewmodel


import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.base.route.toDirectionsRoutes
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
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.*


class MapHomeViewModel(application: Application): ViewModel() {

    //List user
    private val _listSmallDriver = MutableLiveData<MutableList<Driver>>()
    val listSmallDriver: LiveData<MutableList<Driver>> get() = _listSmallDriver

    //GPS location
    private val _pointGPS = MutableLiveData<PointAnnotation>()
    val pointGPS: LiveData<PointAnnotation> get() = _pointGPS


    //Variable to setCamera
    private val _isNecessaryCamera = MutableLiveData<Boolean>()
    val isNecessaryCamera: LiveData<Boolean> get() = _isNecessaryCamera


    //State charging price
    private val _stateChargingPrice = MutableLiveData<StateConstants>()
    val stateChargingPrice: LiveData<StateConstants> get() = _stateChargingPrice


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




    //Points
    private val _pointUbic = MutableLiveData<PointAnnotation>()
    val pointUbic: LiveData<PointAnnotation> get() = _pointUbic

    private val _pointDest = MutableLiveData<PointAnnotation>()
    val pointDest: LiveData<PointAnnotation> get() = _pointDest


    //Routes
    private val _routeState = MutableLiveData<StateConstants>()
    val routeState: LiveData<StateConstants> get() = _routeState

    private val _route = MutableLiveData<List<NavigationRoute>>()
    val route: LiveData<List<NavigationRoute>> get() = _route



    //Recycler information
    fun getDriverProv() {
        _latitudeClient.value?.let {
            _longitudeClient.value?.let { it1 ->

                //Call
                val call = driverDataSourceNetwork.getDriver(
                    Constants.PHP_TOKEN,
                )
                getResponseInfoDriverProv(
                    call,
                    it,
                    it1
                )
            }
        }

    }

    //Get the response about the Driver info
    private fun getResponseInfoDriverProv(call: Call<List<Driver>>,latUser: Double, longUser: Double) {
        call.enqueue(object : Callback<List<Driver>> {
            override fun onResponse(
                call: Call<List<Driver>>,
                response: Response<List<Driver>>
            ) {
                if (response.isSuccessful) {
                    filterDriver(response.body()?.toMutableList(),latUser,longUser)
                } else {
                    _state.value = StateConstants.ERROR
                }
            }

            override fun onFailure(call: Call<List<Driver>>, t: Throwable) {
                _state.value = StateConstants.ERROR
            }
        })
    }

    private fun filterDriver(alDriver: MutableList<Driver>?,latUser: Double, longUser: Double){

        val alResult = alDriver?.filter {
            it.maxDist > calculateDist(latUser ,longUser, it.latitude, it.longitude)
                    && it.latitude != 0.0 && it.longitude != 0.0
        }?.toMutableList()


        _listSmallDriver.value = alResult
        _state.value = StateConstants.SUCCESS
    }

    private fun calculateDist(latUser: Double, longUser: Double, latCar: Double, longCar: Double): Double {

        val earthRadius = 6371.0 // Radio de la Tierra en km
        val dLat = (latCar - latUser) * PI / 180.0
        val dLon = (longCar - longUser) * PI / 180.0
        val lat1Rad = latUser * PI / 180.0
        val lat2Rad = latCar * PI / 180.0

        val a = sin(dLat / 2).pow(2) + sin(dLon / 2).pow(2) * cos(lat1Rad) * cos(lat2Rad)
        val c = 2 * asin(sqrt(a))
        return earthRadius * c
    }




    //Routing
    fun getRouteToDraw(
        originPoint: Point,
        destinationPoint: Point,
        mapboxNavigation: MapboxNavigation
    ) {
        _routeState.value = StateConstants.LOADING
        mapboxNavigation.requestRoutes(
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .coordinatesList(listOf(originPoint, destinationPoint))
                .build(),
            object : NavigationRouterCallback {
                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
                    _routeState.value = StateConstants.ERROR
                }

                override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
                    _routeState.value = StateConstants.ERROR
                }

                override fun onRoutesReady(
                    routes: List<NavigationRoute>,
                    routerOrigin: RouterOrigin
                ) {
                    _route.value = routes
                    _routeState.value = StateConstants.SUCCESS
                }

            }
        )
    }


    //SetLocations
    fun setLatitudeClient(lat: Double){
        _latitudeClient.value = lat
    }

    fun setLongitudeClient(long: Double){
        _longitudeClient.value = long
    }




    fun setIsNecessaryCamera(boolean: Boolean){
        _isNecessaryCamera.value = boolean
    }




    fun startSearchDrivers(){
        viewModelScope.launch {
            while (true){
                Log.d("TEST", "...")
                getDriverProv()
                delay(TimeUnit.SECONDS.toMillis(30))
            }
        }
    }



    //SetPoints
    fun setPointLocation(point: PointAnnotation){
        _pointUbic.value = point
    }

    fun setPointDest(point: PointAnnotation){
        _pointDest.value = point
    }



}


class MapHomeViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapHomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapHomeViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown viewModel class")
    }
}