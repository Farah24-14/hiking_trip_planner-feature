package com.usj.hikingplanner

import com.usj.hikingplanner.data.Difficulty
import com.usj.hikingplanner.ui.create.CreateTripUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Checks the "required information must be present before saving" rule from the brief. */
class CreateTripValidationTest {

    private val today = LocalDate.of(2026, 11, 8)

    private fun validState() = CreateTripUiState(
        routeName = "Cedar Forest Loop",
        date = today.plusDays(3),
        difficulty = Difficulty.MODERATE,
        today = today,
    )

    @Test
    fun completeForm_isValid() {
        assertTrue(validState().isValid)
    }

    @Test
    fun emptyForm_isInvalid_withAnErrorForEachRequiredField() {
        val state = CreateTripUiState(today = today)
        assertFalse(state.isValid)
        assertNotNull(state.routeNameError)
        assertNotNull(state.dateError)
        assertNotNull(state.difficultyError)
    }

    @Test
    fun blankRouteName_isInvalid() {
        assertNotNull(validState().copy(routeName = "   ").routeNameError)
    }

    @Test
    fun pastDate_isInvalid() {
        assertNotNull(validState().copy(date = today.minusDays(1)).dateError)
    }

    @Test
    fun todayIsAllowed() {
        assertNull(validState().copy(date = today).dateError)
    }

    @Test
    fun addingNewItem_appendsItTicked_andClearsTheField() {
        val after = validState().copy(newItemText = "  First aid kit ").withNewItemAdded()
        val added = after.items.last()
        assertEquals("First aid kit", added.name)
        assertTrue(added.included)
        assertTrue(added.isCustom)
        assertEquals("", after.newItemText)
    }

    @Test
    fun addingExistingItem_reTicksIt_insteadOfDuplicating() {
        val start = validState()
        val waterUnticked = start.copy(
            items = start.items.map { if (it.name == "Water") it.copy(included = false) else it },
            newItemText = "water",
        )
        val after = waterUnticked.withNewItemAdded()
        assertEquals(start.items.size, after.items.size)
        assertTrue(after.items.first { it.name == "Water" }.included)
        assertEquals("", after.newItemText)
    }

    @Test
    fun addingBlankItem_changesNothing() {
        val start = validState().copy(newItemText = "   ")
        assertEquals(start, start.withNewItemAdded())
    }

    @Test
    fun checklistWithNothingSelected_isInvalid() {
        val state = validState()
        val nothingSelected = state.copy(items = state.items.map { it.copy(included = false) })
        assertNotNull(nothingSelected.checklistError)
        assertFalse(nothingSelected.isValid)
    }
}
