package com.usj.hikingplanner.data

/** How hard a hike is. Room stores it in the database as its name ("EASY", "MODERATE", "HARD"). */
enum class Difficulty(val label: String) {
    EASY("Easy"),
    MODERATE("Moderate"),
    HARD("Hard"),
}
