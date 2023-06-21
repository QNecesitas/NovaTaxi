package com.qnecesitas.novataxiapp.model

data class Driver(
    val email: String,
    val name: String,
    val phone:String,
    val price:Double,
    val typeCar:String,
    val cantSeat:Int,
    val maxDist: Int,
    val fk_prov: String,
    val lat: Double,
    val long: Double
)