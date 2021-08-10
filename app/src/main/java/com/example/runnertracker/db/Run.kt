package com.example.runnertracker.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "runs_table")
data class Run(
    var img: Bitmap? = null,
    var timeStamp: Long = 0L,
    var avgSpeedInKMPH: Float = 0f,
    var distanceInMeters: Int = 0,
    var timeInMillis: Long = 0L,
    var caloriesBurnt: Int = 0
) {
    @PrimaryKey(autoGenerate = true) var id: Int? = null
}