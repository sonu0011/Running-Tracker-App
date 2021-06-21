package com.sonu.runningtrackerapp.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonu.runningtrackerapp.db.entities.Run
import com.sonu.runningtrackerapp.repositories.MainRepository
import com.sonu.runningtrackerapp.util.SortType
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
) : ViewModel() {

    private val runsSortedByDate = mainRepository.getAllRunsSortedByDate()
    private val runsSortedByAvgSpeed = mainRepository.getAllRunsSortedByAvgSpeed()
    private val runsSortedByCaloriesBurned = mainRepository.getAllRunsSortedByCaloriesBurned()
    private val runsSortedByDistance = mainRepository.getAllRunsSortedByDistance()
    private val runsSortedByTimeInMills = mainRepository.getAllRunsSortedByTimeInMillis()

    var sortType = SortType.SORT_DATE
    val runs = MediatorLiveData<List<Run>>()

    init {
        runs.addSource(runsSortedByDate, Observer { result ->
            if (sortType == SortType.SORT_DATE) {
                result?.let {
                    runs.value = it
                }
            }
        })

        runs.addSource(runsSortedByAvgSpeed, Observer { result ->
            if (sortType == SortType.SORT_AVG_SPEED) {
                result?.let {
                    runs.value = it
                }
            }
        })

        runs.addSource(runsSortedByCaloriesBurned, Observer { result ->
            if (sortType == SortType.SORT_CALORIES_BURNED) {
                result?.let {
                    runs.value = it
                }
            }
        })

        runs.addSource(runsSortedByDistance, Observer { result ->
            if (sortType == SortType.SORT_DISTANCE) {
                result?.let {
                    runs.value = it
                }
            }
        })

        runs.addSource(runsSortedByTimeInMills, Observer { result ->
            if (sortType == SortType.SORT_TIME_IN_MILLS) {
                result?.let {
                    runs.value = it
                }
            }
        })
    }

    fun sorts(sortType: SortType) = when (sortType) {
        SortType.SORT_DATE -> runsSortedByDate.value?.let { runs.value = it }
        SortType.SORT_DISTANCE -> runsSortedByDistance.value?.let { runs.value = it }
        SortType.SORT_CALORIES_BURNED -> runsSortedByCaloriesBurned.value?.let { runs.value = it }
        SortType.SORT_TIME_IN_MILLS -> runsSortedByTimeInMills.value?.let { runs.value = it }
        SortType.SORT_AVG_SPEED -> runsSortedByAvgSpeed.value?.let { runs.value = it }
    }.also {
        this.sortType = sortType
    }

    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }
}