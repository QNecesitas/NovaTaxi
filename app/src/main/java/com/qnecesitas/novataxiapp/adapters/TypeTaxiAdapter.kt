package com.qnecesitas.novataxiapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qnecesitas.novataxiapp.R
import com.qnecesitas.novataxiapp.databinding.RecyclerTypeTaxiBinding
import com.qnecesitas.novataxiapp.model.Vehicle

class TypeTaxiAdapter(
    private val context: Context
) : ListAdapter<Vehicle, TypeTaxiAdapter.VehicleViewHolder>(DiffCallback) {

    private var clickDetails: ITouchDetails? = null
    private var clickAsk: ITouchAsk? = null

    class VehicleViewHolder(private var binding: RecyclerTypeTaxiBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SimpleDateFormat")
        fun bind(
            context: Context,
            vehicle: Vehicle,
            clickDetails: ITouchDetails?,
            clickAsk: ITouchAsk?,
        ) {

            //Declare
            val price = "${vehicle.price} CUP"

            when(vehicle.type){
                "Auto básico" -> binding.image.setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.baseline_drive_eta_24)
                )
                "Auto de confort" -> binding.image.setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.vector_car)
                )
                "Auto familiar" -> binding.image.setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.vector_familiar)
                )
                "Triciclo" -> binding.image.setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.vector_tricycle)
                )
                "Motor" -> binding.image.setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.baseline_directions_bike_24)
                )
                "Bicitaxi" -> binding.image.setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.vector_bicitaxi)
                )
            }

            binding.tvTypeCar.text = vehicle.type
            binding.tvCantPrice.text = price

            binding.tvMoreDetails.setOnClickListener{ clickDetails?.onClickDetails(vehicle) }
            binding.tvBuy.setOnClickListener { clickAsk?.onClickAsk(vehicle) }


        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder{
        val viewHolder = VehicleViewHolder(
            RecyclerTypeTaxiBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
        viewHolder.itemView.setOnClickListener{
            viewHolder.adapterPosition
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        holder.bind(
            context,
            getItem(position),
            clickDetails,
            clickAsk
        )
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Vehicle>(){
            override fun areItemsTheSame(oldItem: Vehicle, newItem: Vehicle): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Vehicle, newItem: Vehicle): Boolean {
                return oldItem == newItem
            }

        }
    }






    //Details
    interface ITouchDetails{
        fun onClickDetails(vehicle: Vehicle)
    }
    fun setClickDetails(clickDetails: ITouchDetails){
        this.clickDetails = clickDetails
    }

    //Ask
    interface ITouchAsk{
        fun onClickAsk(vehicle: Vehicle)
    }
    fun setClick(clickAsk: ITouchAsk){
        this.clickAsk = clickAsk
    }




}