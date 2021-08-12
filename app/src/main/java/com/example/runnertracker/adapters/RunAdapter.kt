package com.example.runnertracker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runnertracker.databinding.RunItemBinding
import com.example.runnertracker.db.Run
import com.example.runnertracker.other.TrackingUtility
import kotlinx.android.synthetic.main.run_item.view.*
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter : RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    inner class RunViewHolder(binding: RunItemBinding) : RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<Run>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder(
            RunItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = differ.currentList[position]

        holder.itemView.apply {
            Glide.with(this).load(run.img).into(ivRunMapImg)

            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timeStamp
            }

            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            tvRunDate.text = dateFormat.format(calendar.time)

            tvRunTime.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

            val avgSpeed = "${run.avgSpeedInKMPH}km/h"
            tvRunSpeed.text = avgSpeed

            val distanceInKM = "${run.distanceInMeters / 1000f}km"
            tvRunDistance.text = distanceInKM

            val calories = "${run.caloriesBurnt}kcal"
            tvRunCalories.text = calories
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

}