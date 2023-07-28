package com.qnecesitas.novataxiapp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.qnecesitas.novataxiapp.auxiliary.NetworkTools
import com.qnecesitas.novataxiapp.auxiliary.UserAccountShared
import com.qnecesitas.novataxiapp.databinding.ActivityUserSettingsBinding
import com.qnecesitas.novataxiapp.databinding.LiEmailToRecoverBinding
import com.qnecesitas.novataxiapp.databinding.LiTermsBinding
import com.qnecesitas.novataxiapp.viewmodel.UserSettingsViewModel
import com.qnecesitas.novataxiapp.viewmodel.UserSettingsViewModelFactory
import com.shashank.sony.fancytoastlib.FancyToast

@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
class ActivityUserSettings : AppCompatActivity() {
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

        viewModel.stateOperation.observe(this) {
            when (it) {
                UserSettingsViewModel.StateConstants.LOADING -> binding.loadingAccount.visibility =
                    View.VISIBLE

                UserSettingsViewModel.StateConstants.SUCCESS -> {
                    binding.loadingAccount.visibility = View.GONE
                    FancyToast.makeText(
                        this, getString(R.string.operacion_realizada_con_exito),
                        FancyToast.LENGTH_LONG,
                        FancyToast.SUCCESS, false
                    ).show()

                }

                UserSettingsViewModel.StateConstants.ERROR -> {
                    NetworkTools.showAlertDialogNoInternet(this)
                    binding.loadingAccount.visibility = View.GONE
                }

            }
        }

        //get User Information
        UserAccountShared.getUserEmail(this)?.let { viewModel.getUserInformationAll(it) }

        //Accept Edit Account
        binding.acceptAccount.setOnClickListener {
            if (isEntryValid()) {
                UserAccountShared.getUserEmail(this)?.let { it1 ->
                    showAlertConfirm(
                        it1,
                        binding.TIETPhone.text.toString(),
                        binding.TIETPassword.text.toString()
                    )
                }
            }
        }
        //About Us
        binding.clAboutUs.setOnClickListener {
            val intent = Intent(this, ActivityAboutUs::class.java)
            startActivity(intent)
        }

        //About Dev
        binding.clAboutDevelopers.setOnClickListener {
            val intent = Intent(this, ActivityAboutDev::class.java)
            startActivity(intent)
        }

        binding.clTermCondc.setOnClickListener {
            liTermsConditions()
        }

        binding.clSignOf.setOnClickListener {
            signOf()
        }
        binding.clDeleteAccount.setOnClickListener {
            deleteUsers()
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

        if (binding.TIETPassword.text.toString().isNotBlank()) {
            binding.TIETPassword.error = null
        } else {
            binding.TIETPassword.error = getString(R.string.Este_campo_no_debe)
            result = false
        }

        if (binding.TIETConfirmPassword.text.toString().isNotBlank()) {
            binding.TIETConfirmPassword.error = null
        } else {
            binding.TIETConfirmPassword.error = getString(R.string.Este_campo_no_debe)
            result = false
        }

        if (binding.TIETConfirmPassword.text.toString() == binding.TIETPassword.text.toString() && binding.TIETPassword.text.toString()
                .isNotBlank()
        ) {
            binding.TIETConfirmPassword.error = null
        } else {
            binding.TIETConfirmPassword.error = getString(R.string.contraseña_no_coincide)
            result = false
        }



        return result
    }

    //Message Date Correct
    private fun showAlertConfirm(email: String, phone: String, password: String) {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
            .setTitle(getString(R.string.actualizar_cuenta))
            .setMessage(getString(R.string.est_s_seguro_de_actualizar_los_datos_de_tu_cuenta))
            .setPositiveButton(R.string.aceptar) { dialog, _ ->
                dialog.dismiss()
                viewModel.updateUser(email, phone, password)
                finish()
            }
            .setNegativeButton(R.string.cancelar) { dialog, _ ->
                dialog.dismiss()
            }

        //create the alert dialog and show it
        builder.create().show()
    }

    private fun signOf() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
            .setTitle(getString(R.string.cerrar_sesi_n_))
            .setMessage(getString(R.string.est_s_seguro_de_cerrar_su_sesi_n))
            .setPositiveButton(R.string.aceptar) { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(this, ActivityLogin::class.java)
                startActivity(intent)
                UserAccountShared.setUserEmail(null, this)
            }
            .setNegativeButton(R.string.cancelar) { dialog, _ ->
                dialog.dismiss()
            }

        //create the alert dialog and show it
        builder.create().show()
    }

    private fun deleteUsers() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
            .setTitle(getString(R.string.eliminar_cuenta1))
            .setMessage(getString(R.string.est_s_seguro_de_eliminar_la_cuenta_))
            .setPositiveButton(R.string.aceptar) { dialog, _ ->
                dialog.dismiss()
                viewModel.listUser.value?.get(0)?.let { viewModel.deleteUsers(it.email) }
                val intent = Intent(this, ActivityLogin::class.java)
                startActivity(intent)
                UserAccountShared.setUserEmail(null, this)
            }
            .setNegativeButton(R.string.cancelar) { dialog, _ ->
                dialog.dismiss()
            }

        //create the alert dialog and show it
        builder.create().show()
    }

    private fun liTermsConditions(){
        val inflater = LayoutInflater.from(binding.root.context)
        val liBinding = LiTermsBinding.inflate(inflater)
        val builder = androidx.appcompat.app.AlertDialog.Builder(binding.root.context)
        builder.setView(liBinding.root)
        val alertDialog = builder.create()


        liBinding.liTIVCerrar.setOnClickListener {
            alertDialog.dismiss()
        }


        //Finish
        builder.setCancelable(true)
        alertDialog.window!!.setGravity(Gravity.CENTER)
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
    }

}
