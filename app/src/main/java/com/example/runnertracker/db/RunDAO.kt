package com.example.runnertracker.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RunDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    ////////////////////////////////////////////////////////////////////////////////

    @Delete
    suspend fun deleteRun(run: Run)

    ////////////////////////////////////////////////////////////////////////////////

    @Query("SELECT * FROM runs_table ORDER BY timeStamp DESC")
    fun getAllRunsSortedByDate(): LiveData<List<Run>>

    @Query("SELECT * FROM runs_table ORDER BY timeInMillis DESC")
    fun getAllRunsSortedByTimeInMillis(): LiveData<List<Run>>

    @Query("SELECT * FROM runs_table ORDER BY caloriesBurnt DESC")
    fun getAllRunsSortedByCaloriesBurnt(): LiveData<List<Run>>

    @Query("SELECT * FROM runs_table ORDER BY distanceInMeters DESC")
    fun getAllRunsSortedByDistance(): LiveData<List<Run>>

    @Query("SELECT * FROM runs_table ORDER BY avgSpeedInKMPH DESC")
    fun getAllRunsSortedByAvgSpeed(): LiveData<List<Run>>

//////////////////////////////////////////////////////////////////////////

    @Query("SELECT SUM(timeInMillis) FROM runs_table")
    fun getTotalTimeInMillis(): LiveData<Long>

    @Query("SELECT SUM(caloriesBurnt) FROM runs_table")
    fun getTotalCaloriesBurnt(): LiveData<Int>

    @Query("SELECT SUM(distanceInMeters) FROM runs_table")
    fun getTotalDistance(): LiveData<Int>

    @Query("SELECT SUM(avgSpeedInKMPH) FROM runs_table")
    fun getTotalAvgSpeedInKNMPH(): LiveData<Float>

}