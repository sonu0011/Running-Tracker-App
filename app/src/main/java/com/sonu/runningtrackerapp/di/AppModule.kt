package com.sonu.runningtrackerapp.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.sonu.runningtrackerapp.db.RunDatabase
import com.sonu.runningtrackerapp.util.Constant.KEY_FIRST_APP_LAUNCH
import com.sonu.runningtrackerapp.util.Constant.KEY_NAME
import com.sonu.runningtrackerapp.util.Constant.KEY_WEIGHT
import com.sonu.runningtrackerapp.util.Constant.RUNNING_DATABASE_NAME
import com.sonu.runningtrackerapp.util.Constant.SHARED_PREF_NAME
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

    @Singleton
    @Provides
    fun provideSharedPreference(@ApplicationContext context: Context) =
        context.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideName(sharedPreferences: SharedPreferences) =
        sharedPreferences.getString(KEY_NAME, "")

    @Singleton
    @Provides
    fun provideWeight(sharedPreferences: SharedPreferences) =
        sharedPreferences.getFloat(KEY_WEIGHT, 80f)

    @Singleton
    @Provides
    fun providesFirstTimeAppLaunch(sharedPreferences: SharedPreferences) =
        sharedPreferences.getBoolean(KEY_FIRST_APP_LAUNCH, true)

}