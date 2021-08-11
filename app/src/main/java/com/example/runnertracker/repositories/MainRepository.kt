package com.example.runnertracker.repositories

import com.example.runnertracker.db.Run
import com.example.runnertracker.db.RunDAO
import javax.inject.Inject

class MainRepository @Inject constructor(private val runDAO: RunDAO) {

    /////////////////////////////////<INSERTION>///////////////////////////////////////////

    suspend fun insertRun(run: Run) = runDAO.insertRun(run)

    ///////////////////////////////////<DELETION>/////////////////////////////////////////

    suspend fun deleteRun(run: Run) = runDAO.deleteRun(run)

    ///////////////////////////////<SORTED DATA>/////////////////////////////////////////////

    fun getAllRunsSortedByDate() = runDAO.getAllRunsSortedByDate()

    fun getAllRunsSortedByTimeInMillis() = runDAO.getAllRunsSortedByTimeInMillis()

    fun getAllRunsSortedByCaloriesBurnt() = runDAO.getAllRunsSortedByCaloriesBurnt()

    fun getAllRunsSortedByDistance() = runDAO.getAllRunsSortedByDistance()

    fun getAllRunsSortedByAvgSpeed() = runDAO.getAllRunsSortedByAvgSpeed()

    /////////////////////////////////<SUM | AVG>///////////////////////////////////////////

    fun getTotalTimeInMillis() = runDAO.getTotalTimeInMillis()

    fun getTotalCaloriesBurnt() = runDAO.getTotalCaloriesBurnt()

    fun getTotalDistance() = runDAO.getTotalDistance()

    fun getTotalAvgSpeedInKNMPH() = runDAO.getTotalAvgSpeedInKNMPH()

}