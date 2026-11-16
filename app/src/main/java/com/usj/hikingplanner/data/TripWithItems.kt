package com.usj.hikingplanner.data

import androidx.room.Embedded
import androidx.room.Relation
import java.time.LocalDate

/** A trip together with its packing checklist, loaded by Room in one query. */
data class TripWithItems(
    @Embedded val trip: TripEntity,
    @Relation(parentColumn = "id", entityColumn = "tripId")
    val items: List<ChecklistItemEntity>,
) {
    val date: LocalDate get() = LocalDate.ofEpochDay(trip.dateEpochDay)
    val sortedItems: List<ChecklistItemEntity> get() = items.sortedBy { it.position }
    val packedCount: Int get() = items.count { it.isPacked }
    val totalCount: Int get() = items.size

    /** A hike can only be marked completed on or after its date, and only once. */
    fun canBeCompleted(today: LocalDate): Boolean = !trip.isCompleted && !date.isAfter(today)

    /** Still in Upcoming although its date has gone by: it is waiting to be marked completed. */
    fun isOverdue(today: LocalDate): Boolean = !trip.isCompleted && date.isBefore(today)

    fun isToday(today: LocalDate): Boolean = !trip.isCompleted && date == today
}
