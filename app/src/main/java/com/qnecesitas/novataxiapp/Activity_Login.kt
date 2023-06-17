package com.qnecesitas.novataxiapp

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.qnecesitas.novataxiapp.auxiliary.Constants
import com.qnecesitas.novataxiapp.auxiliary.NetworkTools
import com.qnecesitas.novataxiapp.data.ModelUser
import com.qnecesitas.novataxiapp.databinding.ActivityLoginBinding
import com.qnecesitas.novataxiapp.network.RetrofitUsersImplS
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Activity_Login : AppCompatActivity() {

    private var binding: ActivityLoginBinding? = null
    private var retrofitUser: RetrofitUsersImplS? = null
    private var al_User: java.util.ArrayList<ModelUser>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        retrofitUser = RetrofitUsersImplS()
        al_User = ArrayList<ModelUser>()


        binding?.btnLogIn?.setOnClickListener(View.OnClickListener { click_login() })

    }

    private fun eventKeyboard(view: View, keyEvent: KeyEvent): Boolean {
        if (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
        return false
    }

    private fun click_login(){
        if(checkIsEmpty()){
            loadUserInternet()
        }else{
            checkPassword()
        }

    }


    private fun loadPasswordInternet() {
        if (NetworkTools.isOnline(this@Activity_Login, false)) {
            val password
            val call: Call<java.util.ArrayList<ModelUser>> =
                retrofitUser?.fetchOrders(Constants.PHP_TOKEN,Constants.VERSION,)
            call.enqueue(object : Callback<java.util.ArrayList<ModelUser?>> {
                override fun onResponse(
                    call: Call<java.util.ArrayList<ModelUser?>>,
                    response: Response<java.util.ArrayList<ModelUser?>>
                ) {
                    binding.ALPBCargando.setVisibility(View.GONE)
                    if (response.isSuccessful) {
                        al_User = response.body()
                        if (al_User != null) {
                            checkPassword()
                        } else {
                            Snackbar.make(
                                binding.ALCLContainerAll,
                                getString(R.string.Revise_su_conexion),
                                Snackbar.LENGTH_LONG
                            ).show()
                            binding.ALPBCargando.setVisibility(View.GONE)
                        }
                    } else {
                        Snackbar.make(
                            binding.ALCLContainerAll,
                            getString(R.string.Revise_su_conexion),
                            Snackbar.LENGTH_LONG
                        ).show()
                        binding.ALPBCargando.setVisibility(View.GONE)
                    }
                }

                override fun onFailure(
                    call: Call<java.util.ArrayList<ModelPassword?>>,
                    t: Throwable
                ) {
                    binding.ALPBCargando.setVisibility(View.GONE)
                    Snackbar.make(
                        binding.ALCLContainerAll,
                        getString(R.string.Revise_su_conexion),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            })
        } else {
            Snackbar.make(
                binding.ALCLContainerAll,
                getString(R.string.Revise_su_conexion),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun checkIsEmpty(): Boolean{
        var amount = 0

        //User
        if(binding?.tietUser?.text!!.trim().isNotEmpty()){
            amount++
        }else{
            binding?.tilUser?.error = getString(R.string.este_campo_no_debe_de_esta_vac_o)
        }

        //Password
        if(binding?.tietPassword?.text!!.trim().isNotEmpty()){
            amount++
        }else{
            binding?.tilPassword?.error = getString(R.string.este_campo_no_debe_de_esta_vac_o)
        }

        return amount == 2
    }

    private fun checkPassword() {
        if (!binding.ALTIETPassword.getText().toString().isEmpty()) {
            val user = if (binding.ALRBAdministrator.isChecked()) "Administrador" else "Dependiente"
            val bdPassword: String
            bdPassword = if (al_password.get(0).getUser().equals(user)) {
                al_password.get(0).getPassword()
            } else {
                al_password.get(1).getPassword()
            }
            val inputPassword: String = binding.ALTIETPassword.getText().toString()
            if (bdPassword == inputPassword) {
                if (user == "Administrador") {
                    loadDeficitInternetS()
                    binding.ALTILPassword.setError(null)
                } else {
                    binding.ALTILPassword.setError(null)
                    binding.ALPBCargando.setVisibility(View.GONE)
                    val intent = Intent(this@Activity_Login, Activity_MenuSelesperson::class.java)
                    startActivity(intent)
                }
            } else {
                countBadPassword++
                binding.ALTILPassword.setError(getString(R.string.Contrasena_incorrecta))
                if (countBadPassword == 5) showAlertDialogClosePassword()
            }
        } else {
            binding.ALTILPassword.setError(getString(R.string.este_campo_no_debe_vacio))
        }
    }


}