package com.example.runnertracker.view_models

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runnertracker.db.Run
import com.example.runnertracker.other.SortType
import com.example.runnertracker.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: MainRepository) : ViewModel() {

    private val runSortedByDate = repository.getAllRunsSortedByDate()
    private val runSortedByAvgSpeed = repository.getAllRunsSortedByAvgSpeed()
    private val runSortedByTime = repository.getAllRunsSortedByTimeInMillis()
    private val runSortedByDistance = repository.getAllRunsSortedByDistance()
    private val runSortedByCalories = repository.getAllRunsSortedByCaloriesBurnt()

    private var sortType = SortType.DATE

    val runs = MediatorLiveData<List<Run>>()

    init {
        runs.addSource(runSortedByDate) { result ->
            if (sortType == SortType.DATE){
                result.let { runs.value = it }
            }
        }
        runs.addSource(runSortedByAvgSpeed) { result ->
            if (sortType == SortType.AVG_SPEED){
                result.let { runs.value = it }
            }
        }
        runs.addSource(runSortedByTime) { result ->
            if (sortType == SortType.TIME){
                result.let { runs.value = it }
            }
        }
        runs.addSource(runSortedByDistance) { result ->
            if (sortType == SortType.DISTANCE){
                result.let { runs.value = it }
            }
        }
        runs.addSource(runSortedByCalories) { result ->
            if (sortType == SortType.CALORIES){
                result.let { runs.value = it }
            }
        }
    }

    fun sortRuns(sortType: SortType) = when (sortType) {
        SortType.DATE -> runSortedByDate.value.let { runs.value = it }
        SortType.TIME -> runSortedByTime.value.let { runs.value = it }
        SortType.AVG_SPEED -> runSortedByAvgSpeed.value.let { runs.value = it }
        SortType.DISTANCE -> runSortedByDistance.value.let { runs.value = it }
        SortType.CALORIES -> runSortedByCalories.value.let { runs.value = it }
    }.also {
        this.sortType = sortType
    }

    fun insertRun(run: Run) = viewModelScope.launch {
        repository.insertRun(run)
    }
}