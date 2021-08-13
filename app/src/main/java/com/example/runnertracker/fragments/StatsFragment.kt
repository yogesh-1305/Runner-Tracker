package com.example.runnertracker.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.runnertracker.R
import com.example.runnertracker.databinding.FragmentStatsBinding
import com.example.runnertracker.other.TrackingUtility
import com.example.runnertracker.view_models.MainViewModel
import com.example.runnertracker.view_models.StatsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.round

@AndroidEntryPoint
class StatsFragment : Fragment() {

    private val viewModel: StatsViewModel by viewModels()
    private lateinit var binding: FragmentStatsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeToObservers()
    }

    @SuppressLint("SetTextI18n")
    private fun subscribeToObservers() {
        viewModel.totalTimeRun.observe(viewLifecycleOwner, {
            it.let {
                val totalTimeRun = TrackingUtility.getFormattedStopWatchTime(it)
                binding.totalTime.text = totalTimeRun
            }
        })

        viewModel.totalDistance.observe(viewLifecycleOwner, {
            it.let {
                val km = it / 1000f
                val totalDistance = round((km * 10f) / 10f)
                binding.totalDistance.text = "$totalDistance km"
            }
        })

        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner, {
            it.let {
                binding.totalDistance.text = "$it kcal"
            }
        })

        viewModel.totalAvgSpeed.observe(viewLifecycleOwner, {
            it.let {
                val avgSpeed = round((it * 10f) / 10f)
                binding.totalAvgSpeed.text = "$avgSpeed km/h"
            }
        })
    }
}