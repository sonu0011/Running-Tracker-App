package com.sonu.runningtrackerapp.repositories

import com.sonu.runningtrackerapp.db.RunDao
import com.sonu.runningtrackerapp.db.entities.Run
import javax.inject.Inject

class MainRepository @Inject constructor(
    val runDao: RunDao
) {
    suspend fun insertRun(run: Run) = runDao.insertRun(run)

    suspend fun deleteRun(run: Run) = runDao.deleteRun(run)

    fun getAllRunsSortedByDate() = runDao.getAllRunsSortedByDate()

    fun getAllRunsSortedByDistance() = runDao.getAllRunsSortedByDistance()

    fun getAllRunsSortedByTimeInMillis() = runDao.getAllRunsSortedByTime()

    fun getAllRunsSortedByAvgSpeed() = runDao.getAllRunsSortedBySpeed()

    fun getAllRunsSortedByCaloriesBurned() = runDao.getAllRunsSortedByCaloriesBurned()

    fun getTotalParams() = runDao.getTotalValues()


}