package com.sonu.runningtrackerapp.util

import android.graphics.Color

object Constant {

    const val RUNNING_DATABASE_NAME = " running_db"
    const val PERMISSION_MESSAGE = "You need to accept permission to use this app"
    const val REQUEST_CODE = 0
    const val PENDING_INTENT_REQUEST_CODE = 0
    const val PAUSE_PENDING_INTENT_REQUEST_CODE = 1
    const val RESUME_PENDING_INTENT_REQUEST_CODE = 2

    const val ACTION_START_RESUME_SERVICE = "START_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val ACTION_LAUNCH_RUN_FRAGMENT = "ACTION_LAUNCH_RUN_FRAGMENT"

    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1
    const val NOTIFICATION_TITLE = "Running App"

    const val SHARED_PREF_NAME = "running_shared_pref"
    const val KEY_NAME = "KEY_NAME"
    const val KEY_WEIGHT = "KEY_WEIGHT"
    const val KEY_FIRST_APP_LAUNCH = "KEY_FIRST_APP_LAUNCH"

    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val FASTEST_LOCATION_INTERVAL = 2000L

    const val POLYLINE_COLOR = Color.RED
    const val POLYLINE_WIDTH = 8f
    const val MAP_ZOOM = 15f

    const val TIMER_UPDATE_CURRENT = 50L

    const val ACTION_DECLARED_FIELD = "mActions"
    const val FRAGMENT_CANCEL_TAG = "FRAGMENT_CANCEL_TAG "

}