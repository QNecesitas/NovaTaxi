package com.qnecesitas.novataxiapp.auxiliary

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import com.qnecesitas.novataxiapp.R

class NetworkTools {

    companion object {

        fun isOnline(context: Context, mostrarAlertDialg: Boolean): Boolean {
            val resutl: Boolean
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val ni = cm.activeNetworkInfo
            resutl = ni != null && ni.isConnected
            if (!resutl && mostrarAlertDialg) {
                showAlertDialogNoInternet(context)
            }
            return resutl
        }

        fun showAlertDialogNoInternet(context: Context) {
            //init alert dialog
            val builder = AlertDialog.Builder(context)
            builder.setCancelable(true)
            builder.setTitle(context.getString(R.string.Se_ha_producido_un_error))
            builder.setMessage(R.string.Revise_su_conexion)
            //set listeners for dialog buttons
            builder.setPositiveButton(
                R.string.Aceptar,
                DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() })

            //create the alert dialog and show it
            builder.create().show()
        }
    }

}