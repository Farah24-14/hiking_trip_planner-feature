package com.usj.hikingplanner

import android.app.Application
import com.usj.hikingplanner.data.HikingDatabase
import com.usj.hikingplanner.data.TripRepository

/** Created once when the app starts; holds the shared repository for all screens. */
class HikingApp : Application() {
    val repository: TripRepository by lazy {
        TripRepository(HikingDatabase.getInstance(this).tripDao())
    }
}
