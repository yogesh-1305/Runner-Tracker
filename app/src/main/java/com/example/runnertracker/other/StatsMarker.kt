package com.example.runnertracker.other

import android.annotation.SuppressLint
import android.content.Context
import com.example.runnertracker.db.Run
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.run_item.view.*
import kotlinx.android.synthetic.main.stats_marker_view.view.*
import java.text.SimpleDateFormat
import java.util.*

class StatsMarker(val runs: List<Run>, context: Context, layoutId: Int): MarkerView(context, layoutId) {

    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())
    }

    @SuppressLint("SetTextI18n")
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)

        if (e == null) {
            return
        }
        val currentRun = e.x.toInt()
        val run = runs[currentRun]

        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timeStamp
        }

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        marker_date.text = "Date: ${dateFormat.format(calendar.time)}"

        marker_time.text = "Time: ${TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)}"

        val avgSpeed = "Avg Speed: ${run.avgSpeedInKMPH}km/h"
        marker_avg_speed.text = avgSpeed

        val distanceInKM = "Distance: ${run.distanceInMeters / 1000f}km"
        marker_distance.text = distanceInKM

        val calories = "Calories: ${run.caloriesBurnt}kcal"
        marker_calories.text = calories
    }
}