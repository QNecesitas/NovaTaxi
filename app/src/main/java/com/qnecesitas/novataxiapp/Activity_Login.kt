package com.qnecesitas.novataxiapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.qnecesitas.novataxiapp.databinding.ActivityLoginBinding

class Activity_Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

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

    }

    private fun clickIntro(){
        if(isInformationGood()){

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