package com.qnecesitas.novataxiapp

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.qnecesitas.novataxiapp.databinding.ActivityCreateAccountBinding
import androidx.activity.viewModels
import com.qnecesitas.novataxiapp.auxiliary.NetworkTools
import com.qnecesitas.novataxiapp.viewmodel.CreateAccountViewModel
import com.qnecesitas.novataxiapp.viewmodel.CreateAccountViewModelFactory

@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
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
        binding.toolbar.setNavigationOnClickListener { showAlertCancel() }


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
                CreateAccountViewModel.StateConstants.DUPLICATED ->{
                    binding.loadingAccount.visibility = View.GONE
                    //init alert dialog
                    val builder = AlertDialog.Builder(this)
                    builder.setCancelable(true)
                    builder.setTitle(getString(R.string.el_correo_ya_existe))
                    builder.setMessage(getString(R.string.ya_existe_una_cuenta_creada_con_ese_correo))
                    //set listeners for dialog buttons
                    builder.setPositiveButton(
                        R.string.Aceptar
                    ) { dialog , _ ->
                        dialog.dismiss()
                    }

                    //create the alert dialog and show it
                    builder.create().show()

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
        val builder = AlertDialog.Builder(this)
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
        val builder = AlertDialog.Builder(this)
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
     private fun showAlertDialogSuccess(email :String) {
        //init alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.confirmar_cuenta))
        builder.setMessage(getString(R.string.hemos_enviado_correo,email))
        //set listeners for dialog buttons
        builder.setPositiveButton(
            R.string.Aceptar
        ) { dialog , _ ->
            dialog.dismiss()
            finish()
        }

         //create the alert dialog and show it
        builder.create().show()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        showAlertCancel()
    }



}