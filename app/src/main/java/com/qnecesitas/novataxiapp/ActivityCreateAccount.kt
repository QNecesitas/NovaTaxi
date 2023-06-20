package com.qnecesitas.novataxiapp

import android.app.AlertDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.qnecesitas.novataxiapp.databinding.ActivityCreateAccountBinding
import androidx.activity.viewModels
import com.qnecesitas.novataxiapp.auxiliary.NetworkTools
import com.qnecesitas.novataxiapp.viewmodel.CreateAccountViewModel
import com.qnecesitas.novataxiapp.viewmodel.CreateAccountViewModelFactory

class ActivityCreateAccount : AppCompatActivity() {

    //Binding
    private lateinit var binding: ActivityCreateAccountBinding

    //ViewModel
    private val viewModel: CreateAccountViewModel by viewModels {
        CreateAccountViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }


        binding.acceptAccount.setOnClickListener {
            if (isEntryValid()) {
                showAlertConfirm()
            }
        }
        binding.cancelAccount.setOnClickListener {
            showAlertCancel()
        }



            //Observes
        viewModel.state.observe(this){
            when(it){
                CreateAccountViewModel.StateConstants.LOADING -> binding.loadingAccount.visibility = View.VISIBLE
                CreateAccountViewModel.StateConstants.SUCCESS -> {
                    binding.loadingAccount.visibility = View.GONE
                    showAlertDialogSuccess(binding.TIETEmail.text.toString())
                }
                CreateAccountViewModel.StateConstants.ERROR -> {
                    NetworkTools.showAlertDialogNoInternet(this)
                    binding.loadingAccount.visibility = View.GONE
                }
            }
        }

    }
      //Date Correct
    private fun isEntryValid(): Boolean {
        var result = true

        if (binding.TIETName.text.toString().trim().isNotEmpty()) {
            binding.TIETName.error = null
        } else {
            binding.TIETName.error = getString(R.string.Este_campo_no_debe)
            result = false
        }

        if (binding.TIETEmail.text.toString().trim().isNotEmpty()) {
            binding.TIETEmail.error = null
        } else {
            binding.TIETEmail.error = getString(R.string.Este_campo_no_debe)
            result = false
        }

        if (binding.TIETPhone.text.toString().trim().isNotEmpty()) {
            binding.TIETPhone.error = null
        } else {
            binding.TIETPhone.error = getString(R.string.Este_campo_no_debe)
            result = false
        }

        if (binding.TIETPassword.text.toString().trim().isNotEmpty() ) {
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
    private fun showAlertConfirm() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setCancelable(true)
            .setTitle(getString(R.string.crear_cuenta))
            .setMessage(getString(R.string.Tiene_seguridad_guardar))
            .setPositiveButton(R.string.aceptar) { dialog, _ ->
                dialog.dismiss()
                sendInformation()
            }
            .setNegativeButton(R.string.cancelar) { dialog, _ ->
                dialog.dismiss()
            }

        //create the alert dialog and show it
        builder.create().show()
    }

     //Message Cancel
    private fun showAlertCancel() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setCancelable(true)
            .setTitle(getString(R.string.cancelar_cuenta))
            .setMessage(getString(R.string.Tiene_seguridad_cancelar))
            .setPositiveButton(R.string.aceptar) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setNegativeButton(R.string.cancelar) { dialog, _ ->
                dialog.dismiss()
            }

        //create the alert dialog and show it
        builder.create().show()
    }
     //Add Date Account
    private fun sendInformation() {
        viewModel.addNewAccountUser(
            binding.TIETName.text.toString(),
            binding.TIETEmail.text.toString(),
            binding.TIETPhone.text.toString(),
            binding.TIETPassword.text.toString()

        )
    }

     //Message Confirm Email
    fun showAlertDialogSuccess(email :String) {
        //init alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.Se_ha_producido_un_error))
        builder.setMessage(getString(R.string.hemos_enviado_correo,email))
        //set listeners for dialog buttons
        builder.setPositiveButton(
            R.string.Aceptar,
            DialogInterface.OnClickListener { dialog , _ ->
                dialog.dismiss()
                finish()
            })

        //create the alert dialog and show it
        builder.create().show()
    }





}