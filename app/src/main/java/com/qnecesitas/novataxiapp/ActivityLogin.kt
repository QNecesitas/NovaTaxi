package com.qnecesitas.novataxiapp

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
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
                checkStateAndGo(it[0].state)
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

    //Click in Start Session
    private fun clickIntro(){
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.tietPassword.windowToken , 0)
        if(isInformationGood()){
            if(NetworkTools.isOnline(this, true)) {
                viewModel.getAppVersion()
            }
        }
    }

    //Check if the information is blank
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

    //Check the state of the account
    private fun checkStateAndGo(state: String){
        when (state){
            "await" -> showAlertDialogNotConfirmed()
            "accepted" -> {
                viewModel.saveUserInfo(
                    viewModel.listUser.value?.get(0),
                    this
                )
                val intent = Intent(this, ActivityMapHome::class.java)
                startActivity(intent)
            }
            "blocked" -> showAlertDialogBlocked()
        }
    }




    //Click in create new Account
    private fun clickNewAccount(){
        val intent = Intent(this, ActivityCreateAccount::class.java)
        startActivity(intent)
    }




    //Click in recover option
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
                showAlertDialogConfirmEmail(liBinding.tiet.text.toString())
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

        builder.setNegativeButton(
            R.string.cancelar
        ) { dialog, _ ->
            dialog.dismiss()
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

    private fun showAlertDialogBlocked() {
        //init alert dialog
        val builder = android.app.AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle(R.string.Cuenta_bloqueada)
        builder.setMessage(getString(R.string.No_podra_usar_app))
        //set listeners for dialog buttons
        builder.setPositiveButton(
            R.string.Aceptar
        ) { dialog, _ ->
            dialog.dismiss()
        }

        //create the alert dialog and show it
        builder.create().show()
    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        showAlertDialogExit()
    }

    private fun showAlertDialogExit() {
        //init alert dialog
        val builder = android.app.AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setTitle(R.string.salir)
        builder.setMessage(R.string.seguro_desea_salir)
        //set listeners for dialog buttons
        builder.setPositiveButton(R.string.Si) { _, _ ->
            //finish the activity
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        builder.setNegativeButton(R.string.No) { dialog, _ ->
            //dialog gone
            dialog.dismiss()
        }
        //create the alert dialog and show it
        builder.create().show()
    }

}