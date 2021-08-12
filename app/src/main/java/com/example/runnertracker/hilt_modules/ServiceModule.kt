package com.example.runnertracker.hilt_modules

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.runnertracker.MainActivity
import com.example.runnertracker.R
import com.example.runnertracker.other.Constants
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Singleton

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @SuppressLint("VisibleForTests")
    @ServiceScoped
    @Provides
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context) =
        FusedLocationProviderClient(context)

    @SuppressLint("UnspecifiedImmutableFlag")
    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(@ApplicationContext context: Context): PendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).also {
                it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )

    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(
        @ApplicationContext context: Context,
        pendingIntent: PendingIntent
    ) = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(false).setOngoing(true)
        .setSmallIcon(R.drawable.ic_baseline_directions_run_24).setContentTitle("Tracking Your Run")
        .setContentText("Tap to see details...")
        .setContentIntent(pendingIntent)
}
