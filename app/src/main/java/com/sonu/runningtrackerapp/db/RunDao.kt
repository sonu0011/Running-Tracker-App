package com.sonu.runningtrackerapp.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sonu.runningtrackerapp.db.entities.Run
import com.sonu.runningtrackerapp.models.TotalValues

@Dao
interface RunDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query("SELECT * FROM run_table ORDER BY timeStamp DESC")
    fun getAllRunsSortedByDate(): LiveData<List<Run>>

    @Query("SELECT * FROM run_table ORDER BY timeInMills DESC")
    fun getAllRunsSortedByTime(): LiveData<List<Run>>

    @Query("SELECT * FROM run_table ORDER BY caloriesBurned DESC")
    fun getAllRunsSortedByCaloriesBurned(): LiveData<List<Run>>

    @Query("SELECT * FROM run_table ORDER BY distanceInMeters DESC")
    fun getAllRunsSortedByDistance(): LiveData<List<Run>>

    @Query("SELECT * FROM run_table ORDER BY avgSpeedInKmPh DESC")
    fun getAllRunsSortedBySpeed(): LiveData<List<Run>>

    @Query("SELECT SUM(timeInMills) AS totalRunTimes , SUM(distanceInMeters) AS totalRunDistance , SUM(caloriesBurned)  AS totalCaloriesBurned, AVG(avgSpeedInKmPh) AS totalAvgSpeed FROM run_table")
    fun getTotalValues(): LiveData<TotalValues>
}