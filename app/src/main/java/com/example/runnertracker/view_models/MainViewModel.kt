package com.example.runnertracker.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runnertracker.db.Run
import com.example.runnertracker.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: MainRepository) : ViewModel() {

    val runSortedByDate = repository.getAllRunsSortedByDate()

    fun insertRun(run: Run) = viewModelScope.launch {
        repository.insertRun(run)
    }
}