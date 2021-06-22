package com.sonu.runningtrackerapp.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.sonu.runningtrackerapp.R
import com.sonu.runningtrackerapp.util.Constant.ACTION_DECLARED_FIELD
import com.sonu.runningtrackerapp.util.Constant.ACTION_PAUSE_SERVICE
import com.sonu.runningtrackerapp.util.Constant.ACTION_START_RESUME_SERVICE
import com.sonu.runningtrackerapp.util.Constant.ACTION_STOP_SERVICE
import com.sonu.runningtrackerapp.util.Constant.FASTEST_LOCATION_INTERVAL
import com.sonu.runningtrackerapp.util.Constant.LOCATION_UPDATE_INTERVAL
import com.sonu.runningtrackerapp.util.Constant.NOTIFICATION_CHANNEL_ID
import com.sonu.runningtrackerapp.util.Constant.NOTIFICATION_CHANNEL_NAME
import com.sonu.runningtrackerapp.util.Constant.NOTIFICATION_ID
import com.sonu.runningtrackerapp.util.Constant.PAUSE_PENDING_INTENT_REQUEST_CODE
import com.sonu.runningtrackerapp.util.Constant.RESUME_PENDING_INTENT_REQUEST_CODE
import com.sonu.runningtrackerapp.util.Constant.TIMER_UPDATE_CURRENT
import com.sonu.runningtrackerapp.util.TrackingUtility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService : LifecycleService() {
    private var isFirstRun = true
    private var serviceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var baseNotificationNBuilder: NotificationCompat.Builder

    private lateinit var currentNotificationBuilder: NotificationCompat.Builder

    //for updating the notification
    private var timeRunsInSeconds = MutableLiveData<Long>()

    private var isTimeEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTime = 0L

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            isTracking.value!!.let {
                result?.locations?.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Timber.d("NEW LOCATION ${location.latitude}  ${location.longitude}")
                    }
                }
            }
        }
    }

    private fun killService() {
        serviceKilled = true
        isFirstRun = true
        postInitialValues()
        pauseService()
        stopForeground(true)
        stopSelf()
    }

    private fun startTimer() {
        isTimeEnabled = true
        addEmptyPolyLine()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()

        CoroutineScope(Dispatchers.Main).launch {

            while (isTracking.value!!) {
                //time diff between now and started
                lapTime = System.currentTimeMillis() - timeStarted
                //post the total run time
                timeRunsInMilliseconds.postValue(timeRun + lapTime)

                if (timeRunsInMilliseconds.value!! >= lastSecondTime + 1000L) {
                    timeRunsInSeconds.postValue(timeRunsInSeconds.value!! + 1)
                    lastSecondTime += 1000L
                }

                delay(TIMER_UPDATE_CURRENT)
            }

            timeRun += lapTime
        }
    }

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()

        //for updating track fragment
        val timeRunsInMilliseconds = MutableLiveData<Long>()
    }

    override fun onCreate() {
        super.onCreate()
        currentNotificationBuilder = baseNotificationNBuilder
        postInitialValues()
        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })
    }

    private fun updateNotificationTrackingState(isTracking: Boolean) {

        val actionText = if (isTracking) "Pause" else "Resume"
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(
                this,
                PAUSE_PENDING_INTENT_REQUEST_CODE,
                pauseIntent,
                FLAG_UPDATE_CURRENT
            )
        } else {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_RESUME_SERVICE
            }
            PendingIntent.getService(
                this,
                RESUME_PENDING_INTENT_REQUEST_CODE,
                pauseIntent,
                FLAG_UPDATE_CURRENT
            )
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        currentNotificationBuilder.javaClass.getDeclaredField(ACTION_DECLARED_FIELD).apply {
            isAccessible = true
            set(currentNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }

        if (!serviceKilled) {
            currentNotificationBuilder = baseNotificationNBuilder.addAction(
                R.drawable.ic_pause_black_24dp,
                actionText,
                pendingIntent
            )
            notificationManager.notify(NOTIFICATION_ID, currentNotificationBuilder.build())
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {

        if (isTracking) {
            if (TrackingUtility.hasLocationPermission(this)) {
                val request = LocationRequest().apply {
                    interval =
                        LOCATION_UPDATE_INTERVAL // set the interval in which you want to get locations.
                    fastestInterval =
                        FASTEST_LOCATION_INTERVAL // if a location is available sooner you can get it
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request, locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {

            when (it.action) {
                ACTION_START_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("resume the service")
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("pause the service")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("stop the service")
                    killService()
                }

            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimeEnabled = false
    }

    private fun postInitialValues() {
        isTracking.value = false
        pathPoints.value = mutableListOf(mutableListOf())
        timeRunsInSeconds.value = 0L
        timeRunsInMilliseconds.value = 0L
    }

    private fun addEmptyPolyLine() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }


    private fun startForegroundService() {
        startTimer()
        isTracking.postValue(true)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(manager)
        }
        startForeground(NOTIFICATION_ID, baseNotificationNBuilder.build())
        timeRunsInSeconds.observe(this, {
            if (!serviceKilled) {
                val notification = currentNotificationBuilder.setContentText(
                    TrackingUtility.getFormattedStopWatchTime(it * 1000L)
                )
                manager.notify(NOTIFICATION_ID, notification.build())
            }

        })


    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(manager: NotificationManager) {
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        manager.createNotificationChannel(notificationChannel)
    }
}