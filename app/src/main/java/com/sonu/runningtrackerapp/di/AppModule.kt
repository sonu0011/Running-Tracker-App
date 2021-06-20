package com.sonu.runningtrackerapp.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.sonu.runningtrackerapp.db.RunDatabase
import com.sonu.runningtrackerapp.util.Constant.RUNNING_DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRunningDb(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, RunDatabase::class.java, RUNNING_DATABASE_NAME).build()


    @Singleton
    @Provides
    fun provideRunningDao(db: RunDatabase) = db.getRunDao()
}