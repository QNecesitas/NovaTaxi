package com.qnecesitas.novataxiapp.viewmodel


import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigation
import com.qnecesitas.novataxiapp.auxiliary.Constants
import com.qnecesitas.novataxiapp.auxiliary.RequestWorker
import com.qnecesitas.novataxiapp.model.Driver
import com.qnecesitas.novataxiapp.network.DriverDataSourceNetwork
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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


    //WorkManger
    private val workManager = WorkManager.getInstance(application)
    val outputWorkList: LiveData<List<WorkInfo>> =
        workManager.getWorkInfosByTagLiveData(Constants.WORKER_DRIVER_CODE)


    //Recycler information
    fun getDriverProv(latUser: Double, longUser: Double) {

        //Call
        val call = driverDataSourceNetwork.getDriver(
            Constants.PHP_TOKEN,
        )
        getResponseInfoDriverProv(call,latUser,longUser)

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
        }?.toMutableList()

        _listSmallDriver.value = alResult
        _state.value = StateConstants.SUCCESS
    }

    private fun calculateDist(latUser: Double, longUser: Double, latCar: Double, longCar: Double): Double {

           return ((sqrt((latUser - latCar) * (latUser - latCar) + (longUser - longCar) * (longUser - longCar)) + 0.004) * 100.0)
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

    private suspend fun getRouteToCalculate(
        originPoint: Point,
        destinationPoint: Point,
        carPoint: Point,
        mapboxNavigation: MapboxNavigation
    ): List<NavigationRoute>? = suspendCoroutine{ continuation ->

        mapboxNavigation.requestRoutes(
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .coordinatesList(listOf( carPoint, originPoint, destinationPoint ))
                .build(),
            object : NavigationRouterCallback {
                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
                    continuation.resume(null)
                }

                override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
                    continuation.resume(null)
                }

                override fun onRoutesReady(
                    routes: List<NavigationRoute>,
                    routerOrigin: RouterOrigin
                ) {
                    continuation.resume(routes)
                }

            }
        )
    }

    private fun getRouteDistance(list: List<NavigationRoute>): Double{
        var sum = 0.0
        for (it in list) {
            sum += it.directionsRoute.distance()
        }
        return  sum
    }

    private suspend fun getPriceDistance(
        driverPrice: Double,
        originPoint: Point,
        destinationPoint: Point,
        carPoint: Point,
        mapboxNavigation: MapboxNavigation): Double?{

        val route =
            getRouteToCalculate(originPoint, destinationPoint, carPoint, mapboxNavigation)
        val distance = route?.let { getRouteDistance(it) }

        return distance?.times(driverPrice)
    }

    fun updatePricesInList(mapboxNavigation: MapboxNavigation){
        try {
            viewModelScope.launch {
                if (_listSmallDriver.value != null) {
                    val newList = mutableListOf<Driver>()
                    for (it in _listSmallDriver.value!!) {
                        it.price = getPriceDistance(
                            it.price,
                            _pointUbic.value!!.point,
                            _pointDest.value!!.point,
                            Point.fromLngLat(it.longitude, it.latitude),
                            mapboxNavigation
                        )!!
                        newList.add(it)
                    }
                    _listSmallDriver.value = newList
                    _stateChargingPrice.value = StateConstants.SUCCESS
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
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



    //Start search drivers
    fun startSearchWork(){
        val operation = OneTimeWorkRequestBuilder<RequestWorker>()
            .addTag(Constants.WORKER_DRIVER_CODE)
            .build()
        workManager.enqueue(
            operation
        )
    }


    //SetPoints
    fun setPointUbic(point: PointAnnotation){
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