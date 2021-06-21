package com.sonu.runningtrackerapp.db.entities

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "run_table")
data class Run(
    var bitMap: Bitmap? = null,
    var timeStamp: Long = 0L,
    var distanceInMeters: Int = 0,
    var avgSpeedInKmPh: Float = 0f,
    var timeInMills: Long = 0L,
    var caloriesBurned: Int = 0
) {

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

}