package com.sonu.runningtrackerapp.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.sonu.runningtrackerapp.R
import com.sonu.runningtrackerapp.ui.MainActivity
import com.sonu.runningtrackerapp.util.Constant
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped


@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @ServiceScoped
    @Provides
    fun provideFusedLocationProvider(@ApplicationContext context: Context) =
        FusedLocationProviderClient(context)

    @ServiceScoped
    @Provides
    fun providePendingIntent(@ApplicationContext context: Context) = PendingIntent.getActivity(
        context,
        Constant.PENDING_INTENT_REQUEST_CODE,
        Intent(context, MainActivity::class.java).also {
            it.action = Constant.ACTION_LAUNCH_RUN_FRAGMENT
        },
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(
        @ApplicationContext context: Context,
        pendingIntent: PendingIntent
    ) =
        NotificationCompat.Builder(context, Constant.NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false) // onclick of notification disable auto hide
            .setOngoing(false) // disable swipe to remove the notificaiton
            .setContentTitle(Constant.NOTIFICATION_TITLE)
            .setContentText("00:00:00")
            .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
            .setContentIntent(pendingIntent)
}