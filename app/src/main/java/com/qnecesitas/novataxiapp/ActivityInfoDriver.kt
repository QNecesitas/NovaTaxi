package com.qnecesitas.novataxiapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.qnecesitas.novataxiapp.auxiliary.NetworkTools
import com.qnecesitas.novataxiapp.databinding.ActivityInfoDriverBinding
import com.qnecesitas.novataxiapp.viewmodel.DriverViewModel
import com.qnecesitas.novataxiapp.viewmodel.DriverViewModelFactory
import com.qnecesitas.novataxiapp.viewmodel.LoginViewModel
import com.qnecesitas.novataxiapp.viewmodel.LoginViewModelFactory

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
                binding.tvPrice.text = viewModel.listDriver.value?.get(0)?.price.toString()
                binding.tvCarType.text = viewModel.listDriver.value?.get(0)?.typeCar
                binding.tvCantSeat.text = viewModel.listDriver.value?.get(0)?.cantSeat.toString()
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
}