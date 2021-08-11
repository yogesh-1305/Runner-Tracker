package com.example.runnertracker.other

import android.graphics.Color

object Constants {

    const val RUNNING_DATABASE_NAME = "runner_database"

    const val REQUEST_CODE_LOCATION_PERMISSIONS = 1001

    const val ACTON_START_OR_RESUME_SERVICE = "ACTON_START_OR_RESUME_SERVICE"
    const val ACTON_PAUSE_SERVICE = "ACTON_PAUSE_SERVICE"
    const val ACTON_STOP_SERVICE = "ACTON_STOP_SERVICE"
    const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"

    const val NOTIFICATION_CHANNEL_ID = "tracking_channel_id"
    const val NOTIFICATION_CHANNEL_NAME = "Location Tracking"
    const val NOTIFICATION_ID = 1

    const val POLY_LINE_COLOR = Color.RED
    const val POLYLINE_WIDTH = 8f
    const val MAP_ZOOM = 15f


    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val FASTEST_LOCATION_UPDATE_INTERVAL = 2000L
}