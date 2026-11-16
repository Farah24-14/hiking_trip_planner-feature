package com.usj.hikingplanner.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.usj.hikingplanner.HikingApp
import com.usj.hikingplanner.ui.create.CreateTripViewModel
import com.usj.hikingplanner.ui.detail.TripDetailViewModel
import com.usj.hikingplanner.ui.triplist.TripListViewModel

/** Tells Compose how to build each screen's ViewModel and gives it the shared repository. */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { TripListViewModel(hikingApp().repository) }
        initializer { CreateTripViewModel(hikingApp().repository) }
        initializer { TripDetailViewModel(createSavedStateHandle(), hikingApp().repository) }
    }
}

private fun CreationExtras.hikingApp(): HikingApp =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HikingApp
