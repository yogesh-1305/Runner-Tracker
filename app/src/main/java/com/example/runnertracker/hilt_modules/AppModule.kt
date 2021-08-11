package com.example.runnertracker.hilt_modules

import android.content.Context
import androidx.room.Room
import com.example.runnertracker.db.Database
import com.example.runnertracker.other.Constants.RUNNING_DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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

}