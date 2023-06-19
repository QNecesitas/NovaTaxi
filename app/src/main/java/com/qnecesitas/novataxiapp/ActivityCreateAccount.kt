package com.qnecesitas.novataxiapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.qnecesitas.novataxiapp.databinding.ActivityCreateAccountBinding
import androidx.activity.viewModels
import com.mapbox.maps.extension.style.expressions.dsl.generated.boolean

class ActivityCreateAccount : AppCompatActivity() {

    private lateinit var binding: ActivityCreateAccountBinding

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
            showAlertDeneg()
        }

    }

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

        if (binding.TIETConfirmPassword.text.toString() == binding.TIETPassword.text.toString()){
            binding.TIETConfirmPassword.error = null
        } else {
            binding.TIETConfirmPassword.error = getString(R.string.contraseña_no_coincide)
            result = false
        }




        return result
    }


    private fun showAlertConfirm() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setCancelable(true)
            .setTitle(getString(R.string.crear_cuenta))
            .setMessage(getString(R.string.Tiene_seguridad_guardar))
            .setPositiveButton(R.string.aceptar) { dialog, _ ->
                dialog.dismiss()
                //TODO saveInformation()
            }
            .setNegativeButton(R.string.cancelar) { dialog, _ ->
                dialog.dismiss()
            }

        //create the alert dialog and show it
        builder.create().show()
    }


    private fun showAlertDeneg() {
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



}