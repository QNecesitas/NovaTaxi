package com.qnecesitas.novataxiapp.auxiliary

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.qnecesitas.novataxiapp.model.Driver
import com.qnecesitas.novataxiapp.network.DriverDataSourceNetwork
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RequestWorker(ctx: Context, params: WorkerParameters) :
    Worker(ctx, params) {

    override fun doWork(): Result {
        return try {
            Log.i("TEST", "WORK")
            Result.success()
        }catch (e: Exception){
            Result.failure()
        }
    }


}