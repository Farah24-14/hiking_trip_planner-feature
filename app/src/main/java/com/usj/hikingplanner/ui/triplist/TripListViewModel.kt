package com.usj.hikingplanner.ui.triplist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.usj.hikingplanner.data.TripRepository
import com.usj.hikingplanner.data.TripWithItems
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Provides the upcoming and completed lists. A null value means "still loading". */
class TripListViewModel(repository: TripRepository) : ViewModel() {

    val upcoming: StateFlow<List<TripWithItems>?> = repository.observeUpcoming()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val completed: StateFlow<List<TripWithItems>?> = repository.observeCompleted()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
