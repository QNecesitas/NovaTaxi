package com.qnecesitas.novataxiapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qnecesitas.novataxiapp.adapters.DriverAdapter.*
import com.qnecesitas.novataxiapp.databinding.RecyclerAvailableTaxiBinding
import com.qnecesitas.novataxiapp.model.Driver

class DriverAdapter(private val context: Context): ListAdapter<Driver, DriverViewHolder>(DiffCallback) {

    private var clickDetails: ITouchDetails? = null
    private var clickAsk: ITouchAsk? = null

    class DriverViewHolder(private var binding: RecyclerAvailableTaxiBinding): RecyclerView.ViewHolder(binding.root){
        @SuppressLint("SimpleDateFormat")
        fun bind(driver: Driver, context: Context,clickDetails:ITouchDetails?,clickAsk:ITouchAsk?) {

            //Declare
            var price = driver.price



            binding.tvPrice.text = price.toString()

            binding.tvMoreDetails.setOnClickListener{ clickDetails?.onClickDetails(position) }
            binding.tvBuy.setOnClickListener{ clickAsk?.onClickAsk(position) }

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DriverViewHolder{
        val viewHolder = DriverViewHolder(
            RecyclerAvailableTaxiBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
        viewHolder.itemView.setOnClickListener{
            val position = viewHolder.adapterPosition
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: DriverViewHolder, position: Int) {
        holder.bind(getItem(position), context,clickDetails,clickAsk)
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Driver>(){
            override fun areItemsTheSame(oldItem: Driver, newItem: Driver): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: Driver, newItem: Driver): Boolean {
                return oldItem == newItem
            }

        }
    }


    //Details
    interface ITouchDetails{
        fun onClickDetails(position: Int)
    }
    fun setClickDetails(clickDetails: ITouchDetails){
        this.clickDetails = clickDetails
    }

    //Ask
    interface ITouchAsk{
        fun onClickAsk(position: Int)
    }
    fun setClick(clickAsk: ITouchAsk){
        this.clickAsk = clickAsk
    }




}