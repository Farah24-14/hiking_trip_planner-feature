package com.usj.hikingplanner.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * All database reads and writes for trips.
 * Functions returning Flow update the UI automatically whenever the data changes.
 */
@Dao
abstract class TripDao {

    @Transaction
    @Query("SELECT * FROM trips WHERE isCompleted = 0 ORDER BY dateEpochDay ASC, id ASC")
    abstract fun observeUpcoming(): Flow<List<TripWithItems>>

    @Transaction
    @Query("SELECT * FROM trips WHERE isCompleted = 1 ORDER BY completedAtMillis DESC, id DESC")
    abstract fun observeCompleted(): Flow<List<TripWithItems>>

    @Transaction
    @Query("SELECT * FROM trips WHERE id = :tripId")
    abstract fun observeTrip(tripId: Long): Flow<TripWithItems?>

    @Insert
    protected abstract suspend fun insertTrip(trip: TripEntity): Long

    @Insert
    protected abstract suspend fun insertItems(items: List<ChecklistItemEntity>)

    /** Saves a trip and its checklist together: either both are saved or neither is. */
    @Transaction
    open suspend fun insertTripWithItems(trip: TripEntity, itemNames: List<String>): Long {
        val tripId = insertTrip(trip)
        insertItems(
            itemNames.mapIndexed { index, name ->
                ChecklistItemEntity(tripId = tripId, name = name, position = index)
            },
        )
        return tripId
    }

    @Query("UPDATE checklist_items SET isPacked = :packed WHERE id = :itemId")
    abstract suspend fun setItemPacked(itemId: Long, packed: Boolean)

    @Query("UPDATE trips SET isCompleted = 1, completedAtMillis = :completedAtMillis WHERE id = :tripId")
    abstract suspend fun markCompleted(tripId: Long, completedAtMillis: Long)
}
