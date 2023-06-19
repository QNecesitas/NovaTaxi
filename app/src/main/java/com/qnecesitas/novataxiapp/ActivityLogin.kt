package com.qnecesitas.novataxiapp

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.qnecesitas.novataxiapp.auxiliary.NetworkTools
import com.qnecesitas.novataxiapp.databinding.ActivityLoginBinding
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
                TODO( "Ir a activity maps")
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
    }

    private fun clickIntro(){
        if(isInformationGood()){
            if(NetworkTools.isOnline(this, true)) {
                viewModel.getUserWithPassword(
                    binding.tietUser.text.toString(),
                    binding.tietPassword.text.toString()
                )
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

    }

    private fun clickRecover(){

    }


}