package com.qnecesitas.novataxiapp.model

data class Driver(
    val email: String ,
    val name: String ,
    val phone:String ,
    val typeCar:String ,
    val cantSeat:Int ,
    val maxDist: Int ,
    val latitude: Double ,
    val longitude: Double ,
    val password: String ,
    var state: String ,
    val statePhoto: Int,
    val balance: Double,
    var price:Double,
    val rating: Int
)