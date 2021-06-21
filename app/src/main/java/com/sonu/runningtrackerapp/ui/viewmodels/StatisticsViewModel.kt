package com.sonu.runningtrackerapp.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.sonu.runningtrackerapp.repositories.MainRepository

class StatisticsViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
) : ViewModel() {
    val totalRuns = mainRepository.getTotalParams()
    val runsSortedByDate = mainRepository.getAllRunsSortedByDate()
}