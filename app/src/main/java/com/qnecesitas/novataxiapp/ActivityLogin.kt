package com.qnecesitas.novataxiapp

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.qnecesitas.novataxiapp.auxiliary.NetworkTools
import com.qnecesitas.novataxiapp.databinding.ActivityLoginBinding
import com.qnecesitas.novataxiapp.databinding.LiEmailToRecoverBinding
import com.qnecesitas.novataxiapp.viewmodel.LoginViewModel
import com.qnecesitas.novataxiapp.viewmodel.LoginViewModelFactory

@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
class ActivityLogin : AppCompatActivity() {

    //Binding
    private lateinit var binding: ActivityLoginBinding

    //ViewModel
    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory()
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)



        //Listeners
        binding.tvForgotPasswClick.setOnClickListener{
            clickRecover()
        }
        binding.tvSignUp.setOnClickListener{
            clickNewAccount()
        }
        binding.btnLogIn.setOnClickListener{
            clickIntro()
        }


        //Observers
        viewModel.listUser.observe(this) {
            if (it?.isEmpty() == true) {
                binding.tietPassword.error = getString(R.string.usuario_o_contrase_a_incorrectos)
            } else {
                viewModel.saveUserInfo(
                    it?.get(0),
                    this
                )
                val intent = Intent(this, ActivityMapHome::class.java)
                startActivity(intent)
            }
        }

        viewModel.state.observe(this) {
            when(it){
                LoginViewModel.StateConstants.LOADING -> binding.progress.visibility = View.VISIBLE
                LoginViewModel.StateConstants.SUCCESS -> binding.progress.visibility = View.GONE
                LoginViewModel.StateConstants.ERROR -> {
                    NetworkTools.showAlertDialogNoInternet(this)
                    binding.progress.visibility = View.GONE
                }
            }

        }

        viewModel.stateRecover.observe(this){
            when(it){
                LoginViewModel.StateConstants.LOADING -> binding.progress.visibility = View.VISIBLE
                LoginViewModel.StateConstants.SUCCESS -> {
                    binding.progress.visibility = View.GONE
                    showAlertDialogEmailSent()
                }
                LoginViewModel.StateConstants.ERROR -> {
                    NetworkTools.showAlertDialogNoInternet(this)
                    binding.progress.visibility = View.GONE
                }
            }
        }

        viewModel.stateVersion.observe(this){
            Log.i("TEST","OK")
            when(it){
                LoginViewModel.StateConstants.LOADING -> binding.progress.visibility = View.VISIBLE
                LoginViewModel.StateConstants.SUCCESS -> {
                    binding.progress.visibility = View.GONE
                    viewModel.getUserWithPassword(
                        binding.tietUser.text.toString(),
                        binding.tietPassword.text.toString()
                    )
                }
                LoginViewModel.StateConstants.ERROR ->{
                    NetworkTools.showAlertDialogNoInternet(this)
                    binding.progress.visibility = View.GONE
                }
            }
        }

        viewModel.versionResponse.observe(this){
            showAlertDialogNotVersion(it)
        }
    }

    private fun clickIntro(){
        if(isInformationGood()){
            if(NetworkTools.isOnline(this, true)) {
                viewModel.getAppVersion()
            }
        }
    }

    private fun isInformationGood(): Boolean{
        var result = true

        if(binding.tietUser.text.toString().isNotBlank()){
            binding.tietUser.error = null
        }else{
            binding.tietUser.error = getString(R.string.Este_campo_no_debe)
            result = false
        }

        if(binding.tietPassword.text.toString().isNotBlank()){
            binding.tietPassword.error = null
        }else{
            binding.tietPassword.error = getString(R.string.Este_campo_no_debe)
            result = false
        }

        return result
    }

    private fun clickNewAccount(){
        val intent = Intent(this, ActivityCreateAccount::class.java)
        startActivity(intent)
    }

    private fun clickRecover(){
        liRecoverPassword()
    }

    private fun liRecoverPassword() {
        val inflater = LayoutInflater.from(binding.root.context)
        val liBinding = LiEmailToRecoverBinding.inflate(inflater)
        val builder = AlertDialog.Builder(binding.root.context)
        builder.setView(liBinding.root)
        val alertDialog = builder.create()


        liBinding.btnCancel.setOnClickListener{
            alertDialog.dismiss()
        }

        liBinding.btnAccept.setOnClickListener {
            if(liBinding.tiet.text.toString().isNotBlank()){
                showAlertDialogConfirmEmail(liBinding.tiet.toString())
            }else{
                liBinding.tiet.error = getString(R.string.Este_campo_no_debe)
            }
        }


        //Finish
        builder.setCancelable(true)
        builder.setTitle(R.string.Recuperar_contrasena)
        alertDialog.window!!.setGravity(Gravity.CENTER)
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
    }

    private fun showAlertDialogConfirmEmail(email: String) {
        //init alert dialog
        val builder = android.app.AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle(R.string.Enviar_contrasena)
        builder.setMessage(getString(R.string.se_enviara_contrasena, email))
        //set listeners for dialog buttons
        builder.setPositiveButton(
            R.string.Aceptar
        ) { dialog, _ ->
            dialog.dismiss()
            viewModel.sendRecoverPetition(email)
        }

        //create the alert dialog and show it
        builder.create().show()
    }

    private fun showAlertDialogEmailSent() {
        //init alert dialog
        val builder = android.app.AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle(R.string.Correo_enviado)
        builder.setMessage(getString(R.string.se_ha_enviado_contrasena))
        //set listeners for dialog buttons
        builder.setPositiveButton(
            R.string.Aceptar
        ) { dialog, _ ->
            dialog.dismiss()
        }

        //create the alert dialog and show it
        builder.create().show()
    }

    private fun showAlertDialogNotVersion(url: String) {
        //init alert dialog
        val builder = android.app.AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle(R.string.Version_desactualizada)
        builder.setMessage(getString(R.string.version_incorrecta, url))
        //set listeners for dialog buttons
        builder.setPositiveButton(
            R.string.Aceptar
        ) { dialog, _ ->
            dialog.dismiss()
        }

        //create the alert dialog and show it
        builder.create().show()
    }

    private fun showAlertDialogNotConfirmed() {
        //init alert dialog
        val builder = android.app.AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle(R.string.Correo_no_confirmado)
        builder.setMessage(getString(R.string.confirme_su_correo))
        //set listeners for dialog buttons
        builder.setPositiveButton(
            R.string.Aceptar
        ) { dialog, _ ->
            dialog.dismiss()
        }

        //create the alert dialog and show it
        builder.create().show()
    }

}