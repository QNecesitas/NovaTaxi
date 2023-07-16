package com.qnecesitas.novataxiapp.auxiliary

import com.mapbox.geojson.Point
import com.qnecesitas.novataxiapp.model.Trip


class RoutesTools {

    companion object{

        var navigationTrip: Trip? = null

        var latitudeDriver: Double = 0.0

        var longitudeDriver: Double = 0.0

    }

}