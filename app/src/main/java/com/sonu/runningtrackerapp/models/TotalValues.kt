package com.sonu.runningtrackerapp.models

data class TotalValues(
    var totalRunTimes: Long = 0L,
    var totalRunDistance: Int = 0,
    var totalCaloriesBurned: Int = 0,
    var totalAvgSpeed: Int = 0
)