package com.sonu.runningtrackerapp.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.sonu.runningtrackerapp.R
import com.sonu.runningtrackerapp.ui.viewmodels.StatisticsViewModel
import com.sonu.runningtrackerapp.util.TrackingUtility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_statistics.*
import java.lang.Math.round

@AndroidEntryPoint
class StatisticFragment : Fragment(R.layout.fragment_statistics) {
    private val viewModel: StatisticsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        viewModel.totalRuns.observe(viewLifecycleOwner) { totalValues ->
            totalValues?.let {
                val totalTimeRun = TrackingUtility.getFormattedStopWatchTime(it.totalRunTimes)
                tvTotalTime.text = totalTimeRun

                val km = it.totalRunDistance / 1000f
                val totalDistance = round(km * 10f) / 10f
                val totalDistanceString = "${totalDistance}km"
                tvTotalDistance.text = totalDistanceString

                val avgSpeed = round(it.totalAvgSpeed * 10f) / 10f
                val avgSpeedString = "${avgSpeed}km/h"
                tvAverageSpeed.text = avgSpeedString

                val totalCalories = "${it.totalCaloriesBurned}kcal"
                tvTotalCalories.text = totalCalories

            }
        }
    }
}