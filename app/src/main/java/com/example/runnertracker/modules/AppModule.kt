package com.example.runnertracker.modules

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.view.Display
import androidx.room.Room
import com.example.runnertracker.db.Database
import com.example.runnertracker.other.Constants.KEY_FIRST_TIME_USER
import com.example.runnertracker.other.Constants.KEY_NAME
import com.example.runnertracker.other.Constants.KEY_WEIGHT
import com.example.runnertracker.other.Constants.RUNNING_DATABASE_NAME
import com.example.runnertracker.other.Constants.SHARED_PREFERENCES_NAME
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRunningDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
        context, Database::class.java,
        RUNNING_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideDatabaseDao(db: Database) = db.getRunDao()

    @SuppressLint("VisibleForTests")
    @Singleton
    @Provides
    @Named("for_fragment")
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context) =
        FusedLocationProviderClient(context)

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideName(sharedPreferences: SharedPreferences) =
        sharedPreferences.getString(KEY_NAME, "") ?: ""

    @Singleton
    @Provides
    fun provideWeight(sharedPreferences: SharedPreferences) =
        sharedPreferences.getFloat(KEY_WEIGHT, 80f)

    @Singleton
    @Provides
    fun provideFirstTimeAppOpened(sharedPreferences: SharedPreferences) =
        sharedPreferences.getBoolean(KEY_FIRST_TIME_USER, true)
}