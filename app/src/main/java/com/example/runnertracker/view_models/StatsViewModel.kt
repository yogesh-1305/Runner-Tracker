package com.example.runnertracker.view_models

import androidx.lifecycle.ViewModel
import com.example.runnertracker.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(private val repository: MainRepository): ViewModel() {

}