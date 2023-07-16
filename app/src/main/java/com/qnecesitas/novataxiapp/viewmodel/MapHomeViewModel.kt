package com.qnecesitas.novataxiapp.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.toDirectionsRoutes
import com.qnecesitas.novataxiapp.auxiliary.Constants
import com.qnecesitas.novataxiapp.auxiliary.UserAccountShared
import com.qnecesitas.novataxiapp.model.Driver
import com.qnecesitas.novataxiapp.model.Trip
import com.qnecesitas.novataxiapp.model.Vehicle
import com.qnecesitas.novataxiapp.network.AuxiliaryDataSourceNetwork
import com.qnecesitas.novataxiapp.network.DriverDataSourceNetwork
import com.qnecesitas.novataxiapp.network.PricesDataSourceNetwork
import com.qnecesitas.novataxiapp.network.TripsDataSourceNetwork
import com.qnecesitas.novataxidriver.model.Prices
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.*


class MapHomeViewModel: ViewModel() {

    //List user
    private val _listDrivers = MutableLiveData<MutableList<Driver>>()
    val listDrivers: LiveData<MutableList<Driver>> get() = _listDrivers

    private val _listVehicles = MutableLiveData<MutableList<Vehicle>>()
    val listVehicle: LiveData<MutableList<Vehicle>> get() = _listVehicles

    //Progress state
    enum class StateConstants { LOADING, SUCCESS, ERROR }

    private val _state = MutableLiveData<StateConstants>()
    val state: LiveData<StateConstants> get() = _state

    private val _statePrices = MutableLiveData<StateConstants>()
    val statePrices: LiveData<StateConstants> get() = _statePrices

    private val _stateAddTrip = MutableLiveData<StateConstants>()
    val stateAddTrip: LiveData<StateConstants> get() = _stateAddTrip

    private val _stateCancelTrip = MutableLiveData<StateConstants>()
    val stateCancelTrip: LiveData<StateConstants> get() = _stateCancelTrip

    private val _stateRate = MutableLiveData<StateConstants>()
    val stateRate: LiveData<StateConstants> get() = _stateRate

    private val _tripState = MutableLiveData<Trip>()
    val tripState: LiveData<Trip> get() = _tripState

    //Progress state version
    private val _stateVersion = MutableLiveData<LoginViewModel.StateConstants>()
    val stateVersion: LiveData<LoginViewModel.StateConstants> get() = _stateVersion

    //Version response
    private val _versionResponse = MutableLiveData<String>()
    val versionResponse: LiveData<String> get() = _versionResponse

    //Distance
    private var lastDistance = 0.0


    //Network Data Source
    private var auxiliaryDataSourceNetwork: AuxiliaryDataSourceNetwork = AuxiliaryDataSourceNetwork()
    private var driverDataSourceNetwork: DriverDataSourceNetwork = DriverDataSourceNetwork()
    private var pricesDataSourceNetwork: PricesDataSourceNetwork = PricesDataSourceNetwork()
    private var tripsDataSourceNetwork: TripsDataSourceNetwork = TripsDataSourceNetwork()

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
    private val latitudeGPS: LiveData<Double> get() = _latitudeGPS

    //LongitudeClient
    private val _longitudeGPS = MutableLiveData<Double>()
    private val longitudeGPS: LiveData<Double> get() = _longitudeGPS



    //Variable to setCamera
    private val _isNecessaryCamera = MutableLiveData<Boolean>()
    val isNecessaryCamera: LiveData<Boolean> get() = _isNecessaryCamera




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





    //Coroutine
    fun startMainCoroutine(context: Context){
        viewModelScope.launch {
            while (true){
                getDriversAll()
                delay(TimeUnit.SECONDS.toMillis(30))
                getTripState(context)
            }
        }
    }




    //Choice car
    fun getPrices(distance: Double) {
        _statePrices.value = StateConstants.LOADING

        //Call
        val call = pricesDataSourceNetwork.getPricesInformation(
            Constants.PHP_TOKEN,
            1
        )
        getResponsePrices(call, distance)
    }

    //Get the response about the prices
    private fun getResponsePrices(call: Call<List<Prices>>, distance: Double) {
        call.enqueue(object : Callback<List<Prices>> {
            override fun onResponse(
                call: Call<List<Prices>>,
                response: Response<List<Prices>>
            ) {
                if (response.isSuccessful) {
                    _statePrices.value = StateConstants.SUCCESS
                    if(response.body() != null) {
                        makeVehiclesList(response.body()!![0], distance)
                    }
                } else {
                    _statePrices.value = StateConstants.ERROR
                }
            }

            override fun onFailure(call: Call<List<Prices>>, t: Throwable) {
                _statePrices.value = StateConstants.ERROR
            }
        })
    }

    private fun makeVehiclesList(prices: Prices, distance: Double){
        //Simple car
        _listVehicles.value = listOf<Vehicle>().toMutableList()
        _listVehicles.value?.add(
            Vehicle(
                "Auto simple",
                (prices.priceNormalCar * distance).toInt(),
                4,
                "Vehículo sencillo con alrededor de 4 capacidades, ideal para obtener mejores precios"
            )
        )

        //Comfort
        _listVehicles.value?.add(
            Vehicle(
                "Auto de confort",
                (prices.priceComfortCar * distance).toInt(),
                4,
                "Vehículo muy cómodo con alrededor de 4 capacidades y aire acondicionado"
            )
        )

        //Familiar
        _listVehicles.value?.add(
            Vehicle(
                "Auto familiar",
                (prices.priceFamiliarCar * distance).toInt(),
                8,
                "Vehículo cómodo con alrededor de 8 capacidades, ideal para el viaje en familia"
            )
        )

        //Tricycle
        _listVehicles.value?.add(
            Vehicle(
                "Triciclo",
                (prices.priceTricycle * distance).toInt(),
                2,
                "Vehículo triciclo con alrededor de 2 asientos, ideal para viajes cortos"
            )
        )


        _listVehicles.value?.add(
            Vehicle(
                "Motor",
                (prices.priceMotorcycle * distance).toInt(),
                1,
                "Vehículo con solo 1 asiento, ideal para viajes rápidos y sin mucho equipaje"
            )
        )

    }

    fun getRouteDistance(list: List<NavigationRoute>): Double{
        var sum = 0.0
        for (it in list.toDirectionsRoutes()) {

            sum += it.distance()
        }
        lastDistance = sum / 1000
        return  sum / 1000
    }




    //Ask for a taxi
    fun addTrip(context: Context, prices: Double, phone: String, typeCar: String){
        try {
            val call = tripsDataSourceNetwork.addTrip(
                Constants.PHP_TOKEN,
                "no",
                UserAccountShared.getUserEmail(context).toString(),
                prices.toInt(),
                lastDistance,
                makeDate()!!,
                _latitudeDestiny.value!!,
                _longitudeDestiny.value!!,
                _latitudeOrigin.value!!,
                _longitudeOrigin.value!!,
                phone,
                typeCar
            )
            getResponseOperation(call)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun getResponseOperation(call: Call<String>) {
        call.enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                if (response.isSuccessful) {
                    if(response.body() == "Success") {
                        _stateAddTrip.value = StateConstants.SUCCESS
                    }else{
                        _stateAddTrip.value = StateConstants.ERROR
                    }
                } else {
                    _stateAddTrip.value = StateConstants.ERROR
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                _stateAddTrip.value = StateConstants.ERROR
            }
        })
    }

    private fun makeDate(): String? {
        val allDate: String
        val calendar = Calendar.getInstance()
        allDate = SimpleDateFormat("dd-MM-yy hh:mm aa", Locale.getDefault()).format(calendar.time)
        return allDate
    }

    fun cancelTaxiAwait(context: Context){
        val call = UserAccountShared.getUserEmail(context)?.let {
            tripsDataSourceNetwork.deleteTrip(
                Constants.PHP_TOKEN,
                it
            )
        }
        if (call != null) {
            getResponseCancelTaxiAwait(call)
        }
    }

    private fun getResponseCancelTaxiAwait(call: Call<String>) {
        call.enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                if (response.isSuccessful) {
                    if(response.body() == "Success") {
                        _stateCancelTrip.value = StateConstants.SUCCESS
                    }else{
                        _stateCancelTrip.value = StateConstants.ERROR
                    }
                } else {
                    _stateCancelTrip.value = StateConstants.ERROR
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                _stateCancelTrip.value = StateConstants.ERROR
            }
        })
    }

    fun rateTaxi(context: Context, rate: Int){
        val call = driverDataSourceNetwork.rateDriver(
            Constants.PHP_TOKEN,
            rate,
            UserAccountShared.getLastDriver(context)
        )
        getResponseRateTaxi(call)
    }

    private fun getResponseRateTaxi(call: Call<String>) {
        call.enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                if (response.isSuccessful) {
                    if(response.body() == "Success") {
                        _stateRate.value = StateConstants.SUCCESS
                    }else{
                        _stateRate.value = StateConstants.ERROR
                    }
                } else {
                    _stateRate.value = StateConstants.ERROR
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                _stateRate.value = StateConstants.ERROR
            }
        })
    }




    //Await for accepted
    private fun getTripState(context: Context){
        val call = tripsDataSourceNetwork.fetchStateTrip(
            Constants.PHP_TOKEN,
            UserAccountShared.getUserEmail(context)!!
        )
        getResponseTripState(call)
    }

    private fun getResponseTripState(call: Call<Trip>) {
        call.enqueue(object : Callback<Trip> {
            override fun onResponse(
                call: Call<Trip>,
                response: Response<Trip>
            ) {
                if (response.isSuccessful) {
                    _tripState.value = response.body()
                }
            }

            override fun onFailure(call: Call<Trip>, t: Throwable) {

            }
        })
    }




    //Fetch APP_Version
    fun getAppVersion(){
        val call = auxiliaryDataSourceNetwork.fetchVersion(
            Constants.APP_VERSION
        )
        getResponseVersion(call)
    }

    //Get the response is version ok
    private fun getResponseVersion(call: Call<String>){
        call.enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                if (response.isSuccessful) {
                    val result = response.body()
                    if(result == "Success") {
                        _stateVersion.value = LoginViewModel.StateConstants.SUCCESS
                    }else{
                        _versionResponse.value = result
                    }
                } else {
                    _stateVersion.value = LoginViewModel.StateConstants.ERROR
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                _stateVersion.value = LoginViewModel.StateConstants.ERROR
            }
        })
    }

}


class MapHomeViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapHomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapHomeViewModel() as T
        }
        throw IllegalArgumentException("Unknown viewModel class")
    }
}