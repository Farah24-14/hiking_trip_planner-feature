package com.usj.hikingplanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One planned (or finished) hike, stored in the "trips" table.
 *
 * The date is stored as an "epoch day" (days since 1 Jan 1970) so it has no time zone problems;
 * convert with LocalDate.ofEpochDay / LocalDate.toEpochDay.
 */
@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routeName: String,
    val dateEpochDay: Long,
    val difficulty: Difficulty,
    val isCompleted: Boolean = false,
    val completedAtMillis: Long? = null,
)
