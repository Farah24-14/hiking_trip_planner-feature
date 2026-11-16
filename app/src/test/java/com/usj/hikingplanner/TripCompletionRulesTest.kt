package com.usj.hikingplanner

import com.usj.hikingplanner.data.Difficulty
import com.usj.hikingplanner.data.TripEntity
import com.usj.hikingplanner.data.TripWithItems
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** A hike can only be marked completed once its date has arrived. */
class TripCompletionRulesTest {

    private val today = LocalDate.of(2026, 11, 9)

    private fun trip(date: LocalDate, completed: Boolean = false) = TripWithItems(
        trip = TripEntity(
            id = 1,
            routeName = "Cedar Forest Loop",
            dateEpochDay = date.toEpochDay(),
            difficulty = Difficulty.EASY,
            isCompleted = completed,
        ),
        items = emptyList(),
    )

    @Test
    fun futureTrip_cannotBeCompleted() {
        assertFalse(trip(today.plusMonths(5)).canBeCompleted(today))
        assertFalse(trip(today.plusDays(1)).canBeCompleted(today))
    }

    @Test
    fun tripOnItsDate_canBeCompleted() {
        assertTrue(trip(today).canBeCompleted(today))
    }

    @Test
    fun pastTrip_canBeCompleted() {
        assertTrue(trip(today.minusDays(3)).canBeCompleted(today))
    }

    @Test
    fun alreadyCompletedTrip_cannotBeCompletedAgain() {
        assertFalse(trip(today.minusDays(3), completed = true).canBeCompleted(today))
    }

    @Test
    fun overdue_onlyForPastUncompletedTrips() {
        assertTrue(trip(today.minusDays(1)).isOverdue(today))
        assertFalse(trip(today).isOverdue(today))
        assertFalse(trip(today.plusDays(1)).isOverdue(today))
        assertFalse(trip(today.minusDays(1), completed = true).isOverdue(today))
    }
}
