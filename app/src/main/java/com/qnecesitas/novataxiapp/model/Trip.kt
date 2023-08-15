package com.qnecesitas.novataxiapp.model

data class Trip(
    val fk_driver: String,
    val fk_user: String,
    val driverName: String,
    val clientName: String,
    val travelPrice: Double,
    val distance: Double,
    val date: String,
    val latDest: Double,
    val longDest: Double,
    val latOri: Double,
    val longOri: Double,
    val state: String,
    val timeAwait: Int,
    val priceAwait:Double,
    val typeCar: String,
    val numberPlate: String
)