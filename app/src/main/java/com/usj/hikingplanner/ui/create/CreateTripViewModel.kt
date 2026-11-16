package com.usj.hikingplanner.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.usj.hikingplanner.data.Difficulty
import com.usj.hikingplanner.data.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

val DEFAULT_PACKING_ITEMS = listOf("Water", "Snacks", "Hiking shoes", "Jacket", "Power bank")

/** One line of the packing list in the form. [included] = will it be saved with the trip. */
data class ChecklistDraftItem(
    val name: String,
    val included: Boolean = true,
    val isCustom: Boolean = false,
)

/** Everything the Create trip form shows. Validation rules live here so they are easy to test. */
data class CreateTripUiState(
    val routeName: String = "",
    val date: LocalDate? = null,
    val difficulty: Difficulty? = null,
    val items: List<ChecklistDraftItem> = DEFAULT_PACKING_ITEMS.map { ChecklistDraftItem(it) },
    val newItemText: String = "",
    /** Errors are only shown after the user presses Save once, not while they are still typing. */
    val showErrors: Boolean = false,
    val isSaving: Boolean = false,
    val savedTripId: Long? = null,
    val today: LocalDate = LocalDate.now(),
) {
    val routeNameError: String?
        get() = when {
            routeName.isBlank() -> "Route name is required"
            routeName.trim().length > 60 -> "Keep the route name under 60 characters"
            else -> null
        }

    val dateError: String?
        get() = when {
            date == null -> "Pick the hiking date"
            date.isBefore(today) -> "The date can't be in the past"
            else -> null
        }

    val difficultyError: String?
        get() = if (difficulty == null) "Choose a difficulty" else null

    val checklistError: String?
        get() = if (items.none { it.included }) "Select at least one item to pack" else null

    val isValid: Boolean
        get() = routeNameError == null && dateError == null &&
            difficultyError == null && checklistError == null

    /**
     * Adds the typed item to the packing list.
     * - Empty text: nothing happens.
     * - Already in the list (any capitalisation): that item is ticked instead of adding a duplicate,
     *   so typing "water" after unticking "Water" brings it back.
     */
    fun withNewItemAdded(): CreateTripUiState {
        val name = newItemText.trim()
        if (name.isEmpty()) return this
        val existing = items.indexOfFirst { it.name.equals(name, ignoreCase = true) }
        val updatedItems = if (existing >= 0) {
            items.mapIndexed { i, item -> if (i == existing) item.copy(included = true) else item }
        } else {
            items + ChecklistDraftItem(name, included = true, isCustom = true)
        }
        return copy(items = updatedItems, newItemText = "")
    }
}

class CreateTripViewModel(private val repository: TripRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTripUiState())
    val uiState: StateFlow<CreateTripUiState> = _uiState.asStateFlow()

    fun onRouteNameChange(value: String) = _uiState.update { it.copy(routeName = value) }

    fun onDateSelected(date: LocalDate) = _uiState.update { it.copy(date = date) }

    fun onDifficultySelected(difficulty: Difficulty) = _uiState.update { it.copy(difficulty = difficulty) }

    fun onItemToggled(index: Int) = _uiState.update { state ->
        state.copy(
            items = state.items.mapIndexed { i, item ->
                if (i == index) item.copy(included = !item.included) else item
            },
        )
    }

    fun onNewItemTextChange(value: String) = _uiState.update { it.copy(newItemText = value) }

    fun onAddItem() = _uiState.update { it.withNewItemAdded() }

    fun onRemoveItem(index: Int) = _uiState.update { state ->
        state.copy(items = state.items.filterIndexed { i, _ -> i != index })
    }

    fun onSave() {
        val state = _uiState.value
        if (state.isSaving) return
        if (!state.isValid) {
            _uiState.update { it.copy(showErrors = true) }
            return
        }
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val id = repository.createTrip(
                routeName = state.routeName.trim(),
                date = state.date!!,
                difficulty = state.difficulty!!,
                checklist = state.items.filter { it.included }.map { it.name },
            )
            _uiState.update { it.copy(isSaving = false, savedTripId = id) }
        }
    }
}
