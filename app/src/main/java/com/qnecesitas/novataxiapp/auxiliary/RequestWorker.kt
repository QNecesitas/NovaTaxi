package com.qnecesitas.novataxiapp.auxiliary

import android.content.Context
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

            val dataSources = DriverDataSourceNetwork()
            val call = dataSources.getDriver(Constants.PHP_TOKEN)

            call.enqueue(object : Callback<List<Driver>> {
                override fun onResponse(
                    call: Call<List<Driver>>,
                    response: Response<List<Driver>>
                ) {
                    if (response.isSuccessful) {

                        val result = response.body()
                        val data = workDataOf(Constants.WORKER_DRIVER_CODE to result)


                        Result.success(data)
                    } else {
                        Result.failure()
                    }
                }

                override fun onFailure(call: Call<List<Driver>>, t: Throwable) {
                    Result.failure()
                }
            })


            Result.success()
        }catch (e: Exception){
            Result.failure()
        }
    }


}