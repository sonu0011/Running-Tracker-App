package com.sonu.runningtrackerapp.ui.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.sonu.runningtrackerapp.R
import com.sonu.runningtrackerapp.ui.viewmodels.StatisticsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatisticFragment : Fragment(R.layout.fragment_statistics) {
    private val viewModel: StatisticsViewModel by viewModels()

}