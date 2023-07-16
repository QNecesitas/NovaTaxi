package com.qnecesitas.novataxiapp.model

data class Driver(
    val email: String ,
    val password: String ,
    val name: String ,
    val phone:String ,
    val typeCar:String,
    val maxDist: Int ,
    val latitude: Double ,
    val longitude: Double ,
    var state: String ,
    val statePhoto: Int,
    val balance: Double,
    val cantSeat:Int ,
    val rating: Int
)