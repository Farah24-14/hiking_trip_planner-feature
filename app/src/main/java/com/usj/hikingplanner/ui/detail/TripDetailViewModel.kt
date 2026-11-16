package com.usj.hikingplanner.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.usj.hikingplanner.data.TripRepository
import com.usj.hikingplanner.data.TripWithItems
import com.usj.hikingplanner.ui.navigation.Routes
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed interface TripDetailUiState {
    data object Loading : TripDetailUiState
    data object NotFound : TripDetailUiState
    data class Loaded(val trip: TripWithItems) : TripDetailUiState
}

/**
 * Loads one trip straight from the database, so the screen always shows what is saved,
 * including after the app is closed and opened again.
 */
class TripDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: TripRepository,
) : ViewModel() {

    private val tripId: Long = checkNotNull(savedStateHandle[Routes.TRIP_ID_ARG])

    val uiState: StateFlow<TripDetailUiState> = repository.observeTrip(tripId)
        .map { trip -> if (trip == null) TripDetailUiState.NotFound else TripDetailUiState.Loaded(trip) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TripDetailUiState.Loading)

    private val loadedTrip: TripWithItems?
        get() = (uiState.value as? TripDetailUiState.Loaded)?.trip

    /**
     * Saved to the database immediately, so progress survives closing the app.
     * A completed trip is a read-only record, so its checklist can no longer change.
     */
    fun setItemPacked(itemId: Long, packed: Boolean) {
        if (loadedTrip?.trip?.isCompleted != false) return
        viewModelScope.launch { repository.setItemPacked(itemId, packed) }
    }

    /** Ignored unless the hike date has arrived (the button is also disabled before then). */
    fun markCompleted() {
        if (loadedTrip?.canBeCompleted(LocalDate.now()) != true) return
        viewModelScope.launch { repository.markCompleted(tripId) }
    }
}
