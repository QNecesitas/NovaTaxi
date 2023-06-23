package com.qnecesitas.novataxiapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import com.qnecesitas.novataxiapp.databinding.ActivityPutMapBinding
import com.shashank.sony.fancytoastlib.FancyToast

class ActivityPutMap : AppCompatActivity() {

    //Binding
    private lateinit var binding: ActivityPutMapBinding

    //Map
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private var pointSelect: Point? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPutMapBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //Toolbar
        setSupportActionBar(binding.mapToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        binding.mapToolbar.setNavigationOnClickListener { finish() }

        //Add Map Event
        val annotationApi = binding.mapView.annotations
        pointAnnotationManager = annotationApi.createPointAnnotationManager()
        binding.mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
        binding.mapView.getMapboxMap().addOnMapLongClickListener {
                point ->
            addAnnotationToMap(point)

            true
        }
        binding.addLocation.setOnClickListener{
            addLocation()
        }


    }


    private fun addAnnotationToMap(point: Point) {
        bitmapFromDrawableRes(
            this@ActivityPutMap,
            R.drawable.marker_map
        )?.let {
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(it)
                .withIconSize(2.0)
            pointAnnotationManager.deleteAll()
            pointAnnotationManager.create(pointAnnotationOptions)
            pointSelect = point

        }
    }

    private fun bitmapFromDrawableRes(context: Context , @DrawableRes resourceId: Int) =
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
    //Add Location
    private fun addLocation(){

        if(pointSelect == null){
            FancyToast.makeText(this,getString(R.string.no_ha_añadido_su_posicion),FancyToast.LENGTH_LONG,FancyToast.WARNING,false,).show()

        }else{
            val intent = Intent()
            intent.putExtra("longitude", pointSelect!!.longitude())
            intent.putExtra("latitude", pointSelect!!.latitude())
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }



}