package com.qnecesitas.novataxiapp.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation

class PutMapViewModel:ViewModel() {

    //Points
    private val _pointLocation = MutableLiveData<PointAnnotation>()
    val pointLocation: LiveData<PointAnnotation> get() = _pointLocation

    private val _pointGPS = MutableLiveData<PointAnnotation>()
    val pointGPS: LiveData<PointAnnotation> get() = _pointGPS




    //SetPoints
    fun setPointLocation(point: PointAnnotation){
        _pointLocation.value = point
    }

    fun setPointGPS(point: PointAnnotation){
        _pointGPS.value = point
    }




    //Converting image asset
    fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
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