package com.qnecesitas.novataxiapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.base.route.toDirectionsRoutes
import com.mapbox.navigation.core.MapboxNavigation
import com.qnecesitas.novataxiapp.R
import com.qnecesitas.novataxiapp.adapters.DriverAdapter.*
import com.qnecesitas.novataxiapp.databinding.RecyclerAvailableTaxiBinding
import com.qnecesitas.novataxiapp.model.Driver
import com.qnecesitas.novataxiapp.viewmodel.MapHomeViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DriverAdapter(
    private val context: Context,
    private val mapboxNavigation: MapboxNavigation,
    private val pointUser: Point,
    private val pointDestiny: Point
) : ListAdapter<Driver, DriverViewHolder>(DiffCallback) {

    private var clickDetails: ITouchDetails? = null
    private var clickAsk: ITouchAsk? = null

    class DriverViewHolder(private var binding: RecyclerAvailableTaxiBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @OptIn(DelicateCoroutinesApi::class)
        @SuppressLint("SimpleDateFormat")
        fun bind(
            driver: Driver,
            context: Context,
            clickDetails: ITouchDetails?,
            clickAsk: ITouchAsk?,
            mapboxNavigation: MapboxNavigation,
            pointUser: Point,
            pointDestiny: Point
        ) {

            //Declare
            var price = context.getString(R.string.Actualizando)
            val cantSeat = driver.cantSeat


            binding.tvPrice.text = price
            binding.tvCantSeat.text = cantSeat.toString()

            binding.tvMoreDetails.setOnClickListener{ clickDetails?.onClickDetails(layoutPosition) }
            if(price != context.getString(R.string.Actualizando)) {
                binding.tvBuy.setOnClickListener {
                    clickAsk?.onClickAsk(
                        driver,
                        price.toInt()
                    )
                }
            }

            GlobalScope.launch {
                price = getCalculatedPrice(
                    mapboxNavigation,
                    driver,
                    pointUser,
                    pointDestiny
                ).toInt().toString() + "  CUP"

                withContext(Dispatchers.Main) {
                    binding.tvPrice.text = price
                }
            }


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
            for (it in list.toDirectionsRoutes()) {

                sum += it.distance()
            }
            return  sum / 1000
        }

        private suspend fun getPriceDistance(
            driverPrice: Double,
            originPoint: Point,
            destinationPoint: Point,
            carPoint: Point,
            mapboxNavigation: MapboxNavigation
        ): Double?{

            val route =
                getRouteToCalculate(originPoint, destinationPoint, carPoint, mapboxNavigation)
            val distance = route?.let { getRouteDistance(it) }

            return distance?.times(driverPrice)
        }

        private suspend fun getCalculatedPrice(
            mapboxNavigation: MapboxNavigation,
            driver: Driver,
            pointUser: Point,
            pointDestiny: Point
        ): Double {
            return try {
                getPriceDistance(
                    driver.price,
                    pointUser,
                    pointDestiny,
                    Point.fromLngLat(driver.longitude, driver.latitude),
                    mapboxNavigation
                )!!
            }catch (e: Exception){
                0.0
            }
        }


    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DriverViewHolder{
        val viewHolder = DriverViewHolder(
            RecyclerAvailableTaxiBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
        viewHolder.itemView.setOnClickListener{
            val position = viewHolder.adapterPosition
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: DriverViewHolder, position: Int) {
        holder.bind(
            getItem(position),
            context,
            clickDetails,
            clickAsk,
            mapboxNavigation,
            pointUser,
            pointDestiny)
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Driver>(){
            override fun areItemsTheSame(oldItem: Driver, newItem: Driver): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Driver, newItem: Driver): Boolean {
                return oldItem == newItem
            }

        }
    }






    //Details
    interface ITouchDetails{
        fun onClickDetails(position: Int)
    }
    fun setClickDetails(clickDetails: ITouchDetails){
        this.clickDetails = clickDetails
    }

    //Ask
    interface ITouchAsk{
        fun onClickAsk(driver: Driver, price: Int)
    }
    fun setClick(clickAsk: ITouchAsk){
        this.clickAsk = clickAsk
    }




}