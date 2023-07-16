package com.qnecesitas.novataxiapp

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import com.qnecesitas.novataxiapp.auxiliary.NetworkTools
import com.qnecesitas.novataxiapp.databinding.ActivityInfoDriverBinding
import com.qnecesitas.novataxiapp.viewmodel.DriverViewModel
import com.qnecesitas.novataxiapp.viewmodel.DriverViewModelFactory


@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
class ActivityInfoDriver : AppCompatActivity() {

    //Binding
    private lateinit var binding: ActivityInfoDriverBinding

    //ViewModel
    private val viewModel: DriverViewModel by viewModels {
        DriverViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoDriverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Get Email
        val emailSelected = intent.extras?.getString("emailSelected","error")

        //Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }


        viewModel.getDriver(emailSelected.toString())

        //Observer

        viewModel.listDriver.observe(this) {
            binding.tvName.text = viewModel.listDriver.value?.get(0)?.name
            binding.tvPhoneDriver.text = viewModel.listDriver.value?.get(0)?.phone
            binding.tvCarType.text = viewModel.listDriver.value?.get(0)?.typeCar
            binding.tvCantSeat.text = viewModel.listDriver.value?.get(0)?.cantSeat.toString()
            showPhoto()
        }


        viewModel.state.observe(this) {
            when(it){
                DriverViewModel.StateConstants.LOADING -> binding.progress.visibility = View.VISIBLE
                DriverViewModel.StateConstants.SUCCESS -> binding.progress.visibility = View.GONE
                DriverViewModel.StateConstants.ERROR -> {
                    NetworkTools.showAlertDialogNoInternet(this)
                    binding.progress.visibility = View.GONE
                }
            }

        }


    }


    private fun showPhoto(){
        var drawableImage: Drawable? = null
        when(viewModel.listDriver.value?.get(0)?.typeCar){
            "Auto simple" ->
                drawableImage = AppCompatResources.getDrawable(this, R.drawable.baseline_drive_eta_24)!!
            "Auto de confort" ->
                drawableImage = AppCompatResources.getDrawable(this, R.drawable.vector_car)!!
            "Auto familiar" ->
                drawableImage = AppCompatResources.getDrawable(this, R.drawable.vector_familiar)!!
            "Triciclo" ->
                drawableImage = AppCompatResources.getDrawable(this, R.drawable.vector_tricycle)!!
            "Motor" ->
                drawableImage = AppCompatResources.getDrawable(this, R.drawable.baseline_directions_bike_24)!!
        }
        binding.image.setImageDrawable(drawableImage)

    }


}