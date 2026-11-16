package com.usj.hikingplanner.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** The single place the screens go through to read or change trips. */
class TripRepository(private val dao: TripDao) {

    fun observeUpcoming(): Flow<List<TripWithItems>> = dao.observeUpcoming()

    fun observeCompleted(): Flow<List<TripWithItems>> = dao.observeCompleted()

    fun observeTrip(tripId: Long): Flow<TripWithItems?> = dao.observeTrip(tripId)

    suspend fun createTrip(
        routeName: String,
        date: LocalDate,
        difficulty: Difficulty,
        checklist: List<String>,
    ): Long = dao.insertTripWithItems(
        trip = TripEntity(
            routeName = routeName,
            dateEpochDay = date.toEpochDay(),
            difficulty = difficulty,
        ),
        itemNames = checklist,
    )

    suspend fun setItemPacked(itemId: Long, packed: Boolean) = dao.setItemPacked(itemId, packed)

    suspend fun markCompleted(tripId: Long) = dao.markCompleted(tripId, System.currentTimeMillis())
}
