package com.usj.hikingplanner.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** The app's local database file ("hiking.db"). Data stays after the app is closed. */
@Database(
    entities = [TripEntity::class, ChecklistItemEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class HikingDatabase : RoomDatabase() {

    abstract fun tripDao(): TripDao

    companion object {
        @Volatile
        private var instance: HikingDatabase? = null

        fun getInstance(context: Context): HikingDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    HikingDatabase::class.java,
                    "hiking.db",
                ).build().also { instance = it }
            }
    }
}
