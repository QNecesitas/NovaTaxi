package com.qnecesitas.novataxidriver.model

data class Prices(
    val id: Int,
    val priceNormalCar:Int,
    val priceComfortCar:Int,
    val priceFamiliarCar:Int,
    val priceTricycle:Int,
    val priceMotorcycle:Int,
    val priceDelay: Int,
    val delayTime: Int,
    val descPercent:Int
)
