package com.qnecesitas.novataxiapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation

class PutMapViewModel:ViewModel() {

    //Points
    private val _pointUbic = MutableLiveData<PointAnnotation>()
    val pointUbic: LiveData<PointAnnotation> get() = _pointUbic

    private val _pointGPS = MutableLiveData<PointAnnotation>()
    val pointGPS: LiveData<PointAnnotation> get() = _pointGPS

    //SetPoints
    fun setPointUbic(point: PointAnnotation){
        _pointUbic.value = point
    }
    fun setPointGPS(point: PointAnnotation){
        _pointGPS.value = point
    }
}

class PutMapViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PutMapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PutMapViewModel() as T
        }
        throw IllegalArgumentException("Unknown viewModel class")
    }
}