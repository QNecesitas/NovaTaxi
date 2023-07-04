package com.qnecesitas.novataxiapp

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.qnecesitas.novataxiapp.auxiliary.NetworkTools
import com.qnecesitas.novataxiapp.databinding.ActivityUserSettingsBinding
import com.qnecesitas.novataxiapp.viewmodel.UserSettingsViewModel
import com.qnecesitas.novataxiapp.viewmodel.UserSettingsViewModelFactory

@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
class Activity_UserSettings : AppCompatActivity() {
    //Binding
    private lateinit var binding: ActivityUserSettingsBinding

    //ViewModel
    private val viewModel: UserSettingsViewModel by viewModels {
        UserSettingsViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        //Get Email
        val emailSelected = intent.getStringExtra("emailSelected")

        //Observer

        viewModel.listUser.observe(this) {

            binding.TIETPhone.setText(viewModel.listUser.value?.get(0)?.phone)
            binding.TIETPassword.setText(viewModel.listUser.value?.get(0)?.password)
            binding.TIETConfirmPassword.setText(viewModel.listUser.value?.get(0)?.password)
        }

        viewModel.state.observe(this) {
            when (it) {
                UserSettingsViewModel.StateConstants.LOADING -> binding.loadingAccount.visibility =
                    View.VISIBLE

                UserSettingsViewModel.StateConstants.SUCCESS -> binding.loadingAccount.visibility =
                    View.GONE

                UserSettingsViewModel.StateConstants.ERROR -> {
                    NetworkTools.showAlertDialogNoInternet(this)
                    binding.loadingAccount.visibility = View.GONE
                }
            }

        }

         //get User Information
        viewModel.getUserInformationAll(emailSelected.toString())

        //Accept Edit Account
        binding.acceptAccount.setOnClickListener {
            if (isEntryValid()) {
                showAlertConfirm(emailSelected.toString(),binding.TIETPhone.text.toString(),binding.TIETPassword.text.toString())
            }
        }
        //About Us
        binding.aboutUs.setOnClickListener {
            val intent= Intent(this,ActivityAboutUs::class.java)
            startActivity(intent)
        }

        //About Dev
        binding.aboutDev.setOnClickListener {
            val intent = Intent(this,ActivityAboutDev::class.java)
            startActivity(intent)
        }


    }
     //Entry Valid
    private fun isEntryValid(): Boolean {
        var result = true

        if (binding.TIETPhone.text.toString().isNotBlank()) {
            binding.TIETPhone.error = null
        } else {
            binding.TIETPhone.error = getString(R.string.Este_campo_no_debe)
            result = false
        }

        if (binding.TIETPassword.text.toString().isNotBlank() ) {
            binding.TIETPassword.error = null
        } else {
            binding.TIETPassword.error = getString(R.string.Este_campo_no_debe)
            result = false
        }

        if(binding.TIETConfirmPassword.text.toString().isNotBlank()){
            binding.TIETConfirmPassword.error = null
        } else {
            binding.TIETConfirmPassword.error = getString(R.string.Este_campo_no_debe)
            result = false
        }

        if (binding.TIETConfirmPassword.text.toString() == binding.TIETPassword.text.toString() && binding.TIETPassword.text.toString().isNotBlank()){
            binding.TIETConfirmPassword.error = null
        } else {
            binding.TIETConfirmPassword.error = getString(R.string.contraseña_no_coincide)
            result = false
        }



        return result
    }

    //Message Date Correct
    private fun showAlertConfirm(email: String,phone:String,password:String) {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
            .setTitle(getString(R.string.actualizar_cuenta))
            .setMessage(getString(R.string.est_s_seguro_de_actualizar_los_datos_de_tu_cuenta))
            .setPositiveButton(R.string.aceptar) { dialog, _ ->
                dialog.dismiss()
                viewModel.updateUser(email,phone,password)
                finish()
            }
            .setNegativeButton(R.string.cancelar) { dialog, _ ->
                dialog.dismiss()
            }

        //create the alert dialog and show it
        builder.create().show()
    }
}