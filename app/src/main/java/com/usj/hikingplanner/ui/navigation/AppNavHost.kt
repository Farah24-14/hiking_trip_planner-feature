package com.usj.hikingplanner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.usj.hikingplanner.ui.create.CreateTripScreen
import com.usj.hikingplanner.ui.detail.TripDetailScreen
import com.usj.hikingplanner.ui.triplist.TripListMode
import com.usj.hikingplanner.ui.triplist.TripListScreen

/** All the screens of the app and how to move between them. */
object Routes {
    const val UPCOMING = "upcoming"
    const val COMPLETED = "completed"
    const val CREATE = "create"
    const val TRIP_ID_ARG = "tripId"
    const val DETAIL = "trip/{$TRIP_ID_ARG}"

    fun detail(tripId: Long) = "trip/$tripId"
}

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    // Switches between the two bottom tabs without stacking screens on top of each other.
    val openTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(Routes.UPCOMING) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(navController = navController, startDestination = Routes.UPCOMING) {
        composable(Routes.UPCOMING) {
            TripListScreen(
                mode = TripListMode.UPCOMING,
                onOpenTab = openTab,
                onTripClick = { id -> navController.navigate(Routes.detail(id)) },
                onCreateTrip = { navController.navigate(Routes.CREATE) },
            )
        }
        composable(Routes.COMPLETED) {
            TripListScreen(
                mode = TripListMode.COMPLETED,
                onOpenTab = openTab,
                onTripClick = { id -> navController.navigate(Routes.detail(id)) },
                onCreateTrip = { navController.navigate(Routes.CREATE) },
            )
        }
        composable(Routes.CREATE) {
            CreateTripScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument(Routes.TRIP_ID_ARG) { type = NavType.LongType }),
        ) {
            TripDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
