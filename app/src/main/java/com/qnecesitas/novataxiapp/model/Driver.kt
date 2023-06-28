package com.qnecesitas.novataxiapp.model

data class Driver(
    val email: String,
    val name: String,
    val phone:String,
    var price:Double,
    val typeCar:String,
    val cantSeat:Int,
    val maxDist: Int,
    val latitude: Double,
    val longitude: Double
)