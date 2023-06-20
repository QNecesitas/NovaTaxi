package com.qnecesitas.novataxiapp.model

data class Driver(
    val email: String,
    val name: String,
    val phone:String,
    val price:Double,
    val typeCar:String,
    val cantSeat:Int
)