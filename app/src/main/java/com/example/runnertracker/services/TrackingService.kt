package com.example.runnertracker.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.example.runnertracker.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runnertracker.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runnertracker.other.Constants.ACTION_STOP_SERVICE
import com.example.runnertracker.other.Constants.FASTEST_LOCATION_UPDATE_INTERVAL
import com.example.runnertracker.other.Constants.LOCATION_UPDATE_INTERVAL
import com.example.runnertracker.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.runnertracker.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.runnertracker.other.Constants.NOTIFICATION_ID
import com.example.runnertracker.other.Constants.TIMER_UPDATE_INTERVAL
import com.example.runnertracker.other.TrackingUtility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

typealias PolyLine = MutableList<LatLng>
typealias PolyLines = MutableList<PolyLine>

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    private var isFirstRun = true
    private var isServiceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

//    private lateinit var currentNotificationBuilder: NotificationCompat.Builder

    private val timeInSeconds = MutableLiveData<Long>()

    companion object {
        val timeInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathCoordinates = MutableLiveData<PolyLines>()
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        pathCoordinates.postValue(mutableListOf())
        timeInSeconds.postValue(0L)
        timeInMillis.postValue(0L)
    }

    @SuppressLint("VisibleForTests")
    override fun onCreate() {
        super.onCreate()
//        currentNotificationBuilder = baseNotificationBuilder
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, {
            updateLocationTracking(it)
//            updateNotificationTrackingState(it) // code - 001
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var secondLastTimestamp = 0L

    private fun startTimer() {
        addEmptyPolyLine()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                // time diff b/w now and time started
                lapTime = System.currentTimeMillis() - timeStarted
                /// post new lap time
                timeInMillis.postValue(timeRun + lapTime)

                if (timeInMillis.value!! >= secondLastTimestamp + 1000L) {
                    timeInSeconds.postValue(timeInSeconds.value!! + 1)
                    secondLastTimestamp += 1
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermissions(this)) {
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
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            if (isTracking.value!!) {
                p0.locations.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                    }
                }
            }
        }
    }

    private fun addPathPoint(location: Location?) {
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
        startTimer()
        isTracking.postValue(true)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        // code 001
//        timeInSeconds.observe(this, {
//            val notification =
//                currentNotificationBuilder.setContentText(Permissions.getFormattedStopWatchTime(it * 1000L))
//            notificationManager.notify(NOTIFICATION_ID, notification.build())
//        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun killService() {
        isServiceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }

    // used for showing timer in notification code - 001
//    @SuppressLint("UnspecifiedImmutableFlag")
//    private fun updateNotificationTrackingState(isTracking: Boolean) {
//        val notificationText = if (isTracking) "Pause" else "Resume"
//        val pendingIntent = if (isTracking) {
//            val pauseIntent = Intent(this, TrackingService::class.java).apply {
//                action = ACTON_PAUSE_SERVICE
//            }
//            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
//        } else {
//            val pauseIntent = Intent(this, TrackingService::class.java).apply {
//                action = ACTON_START_OR_RESUME_SERVICE
//            }
//            PendingIntent.getService(this, 2, pauseIntent, FLAG_UPDATE_CURRENT)
//        }
//
//        val notificationManager =
//            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        // clearing all the previous actions before updating the notification
//        // to prevent creating a new action everytime the notification in updated
//        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
//            isAccessible = true
//            set(currentNotificationBuilder, ArrayList<NotificationCompat.Action>())
//        }
//
//        currentNotificationBuilder = baseNotificationBuilder.addAction(
//            R.drawable.ic_baseline_pause_24,
//            notificationText,
//            pendingIntent
//        )
//        notificationManager.notify(NOTIFICATION_ID, currentNotificationBuilder.build())
//    }

}