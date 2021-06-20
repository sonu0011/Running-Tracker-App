package com.sonu.runningtrackerapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sonu.runningtrackerapp.db.entities.Run
import com.sonu.runningtrackerapp.util.Converter

@Database(
    entities = [Run::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converter::class)
abstract class RunDatabase  : RoomDatabase(){

    abstract fun getRunDao(): RunDao
}