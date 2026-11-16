package com.usj.hikingplanner.ui.detail

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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.usj.hikingplanner.data.ChecklistItemEntity
import com.usj.hikingplanner.data.TripWithItems
import com.usj.hikingplanner.ui.AppViewModelProvider
import com.usj.hikingplanner.ui.common.pretty
import com.usj.hikingplanner.ui.common.toLocalDateTimeText
import com.usj.hikingplanner.ui.triplist.PackingProgress
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    onBack: () -> Unit,
    viewModel: TripDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val title = (state as? TripDetailUiState.Loaded)?.trip?.trip?.routeName ?: "Trip details"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (val s = state) {
                TripDetailUiState.Loading ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))

                TripDetailUiState.NotFound ->
                    Text("This trip no longer exists.", Modifier.align(Alignment.Center))

                is TripDetailUiState.Loaded -> TripDetailContent(
                    trip = s.trip,
                    onItemChecked = viewModel::setItemPacked,
                    onMarkCompleted = viewModel::markCompleted,
                )
            }
        }
    }
}

@Composable
private fun TripDetailContent(
    trip: TripWithItems,
    onItemChecked: (Long, Boolean) -> Unit,
    onMarkCompleted: () -> Unit,
) {
    var showConfirm by rememberSaveable { mutableStateOf(false) }
    val today = remember { LocalDate.now() }
    val isCompleted = trip.trip.isCompleted
    val canComplete = trip.canBeCompleted(today)

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { TripInfoCard(trip) }

        item {
            Text(
                "Packing checklist",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            PackingProgress(packed = trip.packedCount, total = trip.totalCount)
            if (isCompleted) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "This hike is completed, so its checklist is saved as a record and can't be changed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            OutlinedCard(Modifier.fillMaxWidth()) {
                val items = trip.sortedItems
                items.forEachIndexed { index, item ->
                    ChecklistRow(
                        item = item,
                        enabled = !isCompleted,
                        onCheckedChange = { onItemChecked(item.id, it) },
                    )
                    if (index < items.lastIndex) HorizontalDivider()
                }
            }
        }

        if (!isCompleted) {
            item {
                Button(
                    onClick = { showConfirm = true },
                    enabled = canComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Mark as completed")
                }
                if (!canComplete) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "You can mark this trip completed from ${trip.date.pretty()}, once the hike has happened.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Mark as completed?") },
            text = { Text("\"${trip.trip.routeName}\" will move to Completed trips.") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirm = false
                    onMarkCompleted()
                }) { Text("Mark completed") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun TripInfoCard(trip: TripWithItems) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            InfoRow("Route", trip.trip.routeName)
            InfoRow("Date", trip.date.pretty())
            InfoRow("Difficulty", trip.trip.difficulty.label)
            InfoRow(
                "Status",
                if (trip.trip.isCompleted) {
                    val whenDone = trip.trip.completedAtMillis?.toLocalDateTimeText()
                    if (whenDone != null) "Completed on $whenDone" else "Completed"
                } else {
                    "Upcoming"
                },
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            modifier = Modifier.width(96.dp),
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun ChecklistRow(
    item: ChecklistItemEntity,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = item.isPacked,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = onCheckedChange,
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = item.isPacked, onCheckedChange = null, enabled = enabled)
        Spacer(Modifier.width(8.dp))
        Text(
            item.name,
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = if (item.isPacked) TextDecoration.LineThrough else null,
            color = if (item.isPacked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 12.dp),
        )
    }
}
