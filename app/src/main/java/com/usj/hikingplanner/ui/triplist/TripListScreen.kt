package com.usj.hikingplanner.ui.triplist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.usj.hikingplanner.data.TripWithItems
import com.usj.hikingplanner.ui.AppViewModelProvider
import com.usj.hikingplanner.ui.common.pretty
import com.usj.hikingplanner.ui.navigation.Routes
import java.time.LocalDate

enum class TripListMode { UPCOMING, COMPLETED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripListScreen(
    mode: TripListMode,
    onOpenTab: (String) -> Unit,
    onTripClick: (Long) -> Unit,
    onCreateTrip: () -> Unit,
    viewModel: TripListViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val tripsFlow = if (mode == TripListMode.UPCOMING) viewModel.upcoming else viewModel.completed
    val trips by tripsFlow.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (mode == TripListMode.UPCOMING) "Upcoming trips" else "Completed trips")
                },
            )
        },
        floatingActionButton = {
            if (mode == TripListMode.UPCOMING) {
                ExtendedFloatingActionButton(
                    onClick = onCreateTrip,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Create trip") },
                )
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = mode == TripListMode.UPCOMING,
                    onClick = { onOpenTab(Routes.UPCOMING) },
                    icon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
                    label = { Text("Upcoming") },
                )
                NavigationBarItem(
                    selected = mode == TripListMode.COMPLETED,
                    onClick = { onOpenTab(Routes.COMPLETED) },
                    icon = { Icon(Icons.Filled.CheckCircle, contentDescription = null) },
                    label = { Text("Completed") },
                )
            }
        },
    ) { innerPadding ->
        val list = trips
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                list == null -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                list.isEmpty() -> EmptyState(mode, Modifier.align(Alignment.Center))
                else -> LazyColumn(
                    // Extra bottom space so the last card is not hidden behind the button.
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(list, key = { it.trip.id }) { trip ->
                        TripCard(trip = trip, onClick = { onTripClick(trip.trip.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(mode: TripListMode, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            if (mode == TripListMode.UPCOMING) "No upcoming trips yet" else "No completed hikes yet",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (mode == TripListMode.UPCOMING) {
                "Tap \"Create trip\" to plan your first hike."
            } else {
                "Open an upcoming trip and mark it as completed after your hike."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun TripCard(trip: TripWithItems, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    trip.trip.routeName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                AssistChip(onClick = onClick, label = { Text(trip.trip.difficulty.label) })
            }
            Text(
                trip.date.pretty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val today = remember { LocalDate.now() }
            val note = when {
                trip.isToday(today) -> "Hike day is today!"
                trip.isOverdue(today) -> "Hike date has passed: open it to mark it completed"
                else -> null
            }
            if (note != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    note,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            Spacer(Modifier.height(12.dp))
            PackingProgress(packed = trip.packedCount, total = trip.totalCount)
        }
    }
}

@Composable
fun PackingProgress(packed: Int, total: Int, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            "$packed of $total items packed",
            style = MaterialTheme.typography.labelLarge,
        )
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { if (total == 0) 0f else packed.toFloat() / total },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
