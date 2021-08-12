package com.example.runnertracker.hilt_modules

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Room
import com.example.runnertracker.db.Database
import com.example.runnertracker.other.Constants.RUNNING_DATABASE_NAME
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
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

}