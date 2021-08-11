package com.example.runnertracker.services

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
import com.example.runnertracker.MainActivity
import com.example.runnertracker.R
import com.example.runnertracker.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.runnertracker.other.Constants.ACTON_PAUSE_SERVICE
import com.example.runnertracker.other.Constants.ACTON_START_OR_RESUME_SERVICE
import com.example.runnertracker.other.Constants.ACTON_STOP_SERVICE
import com.example.runnertracker.other.Constants.FASTEST_LOCATION_UPDATE_INTERVAL
import com.example.runnertracker.other.Constants.LOCATION_UPDATE_INTERVAL
import com.example.runnertracker.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.runnertracker.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.runnertracker.other.Constants.NOTIFICATION_ID
import com.example.runnertracker.permissions.Permissions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber

typealias RunLine = MutableList<LatLng>
typealias RunLines = MutableList<RunLine>

class TrackingService : LifecycleService() {

    private var isFirstRun = true

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathCoordinates = MutableLiveData<RunLines>()
    }

    private fun postInitialValues(){
        isTracking.postValue(false)
        pathCoordinates.postValue(mutableListOf())
    }

    @SuppressLint("VisibleForTests")
    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, {
            updateLocationTracking(it)
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTON_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun){
                        startForegroundService()
                        isFirstRun = false
                    }
                    Timber.d("started or resumed service")
                }
                ACTON_PAUSE_SERVICE -> {
                    Timber.d("PAUSED service")
                }
                ACTON_STOP_SERVICE -> {
                    Timber.d("Stopped or resumed service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean){
        if (isTracking){
            if (Permissions.hasLocationPermissions(this)){
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_UPDATE_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        }else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            if (isTracking.value!!){
                p0.locations.let { locations ->
                    for (location in locations){
                        addPathPoint(location)
                        Timber.d("NEW LOCATION: ${location.longitude}")
                    }
                }
            }
        }
    }

    private fun addPathPoint(location: Location?){
        location?.let {
            val position = LatLng(location.latitude, location.longitude)
            pathCoordinates.value?.apply {
                last().add(position)
                pathCoordinates.postValue(this)
            }
        }
    }

    private fun addEmptyPolyLine() = pathCoordinates.value?.apply {
        add(mutableListOf())
        pathCoordinates.postValue(this)
    } ?: pathCoordinates.postValue(mutableListOf(mutableListOf()))

    private fun startForegroundService() {
        addEmptyPolyLine()
        isTracking.postValue(true)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(manager)
        }
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false).setOngoing(true)
            .setSmallIcon(R.drawable.ic_baseline_directions_run_24).setContentTitle("Running App")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
        },
        FLAG_UPDATE_CURRENT
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

}