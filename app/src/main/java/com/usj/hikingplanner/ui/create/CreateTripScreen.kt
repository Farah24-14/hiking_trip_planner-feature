package com.usj.hikingplanner.ui.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.usj.hikingplanner.data.Difficulty
import com.usj.hikingplanner.ui.AppViewModelProvider
import com.usj.hikingplanner.ui.common.pretty
import com.usj.hikingplanner.ui.common.toUtcMillis
import com.usj.hikingplanner.ui.common.utcMillisToLocalDate
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripScreen(
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
    viewModel: CreateTripViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Once the trip is saved, leave the form (the trip now shows in Upcoming trips).
    LaunchedEffect(state.savedTripId) {
        state.savedTripId?.let(onSaved)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create trip") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            RouteNameField(
                value = state.routeName,
                error = state.routeNameError.takeIf { state.showErrors },
                onValueChange = viewModel::onRouteNameChange,
            )
            DateField(
                date = state.date,
                today = state.today,
                error = state.dateError.takeIf { state.showErrors },
                onDateSelected = viewModel::onDateSelected,
            )
            DifficultyField(
                selected = state.difficulty,
                error = state.difficultyError.takeIf { state.showErrors },
                onSelected = viewModel::onDifficultySelected,
            )
            ChecklistField(
                state = state,
                error = state.checklistError.takeIf { state.showErrors },
                onToggle = viewModel::onItemToggled,
                onRemove = viewModel::onRemoveItem,
                onNewItemTextChange = viewModel::onNewItemTextChange,
                onAdd = viewModel::onAddItem,
            )

            if (state.showErrors && !state.isValid) {
                Text(
                    "Please fix the highlighted fields before saving.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Button(
                onClick = viewModel::onSave,
                enabled = !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Save trip")
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun ErrorText(error: String?) {
    if (error != null) {
        Text(
            error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp),
        )
    }
}

@Composable
private fun RouteNameField(value: String, error: String?, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Route name") },
        placeholder = { Text("e.g. Cedar Forest Loop") },
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    date: LocalDate?,
    today: LocalDate,
    error: String?,
    onDateSelected: (LocalDate) -> Unit,
) {
    var showPicker by rememberSaveable { mutableStateOf(false) }

    Column {
        SectionTitle("Date")
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { showPicker = true },
            modifier = Modifier.fillMaxWidth(),
            colors = if (error != null) {
                androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                )
            } else {
                androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
            },
        ) {
            Icon(Icons.Filled.DateRange, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(date?.pretty() ?: "Pick a date")
        }
        ErrorText(error)
    }

    if (showPicker) {
        val todayMillis = remember(today) { today.toUtcMillis() }
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date?.toUtcMillis(),
            // Only today and future days can be picked: this is a planner for upcoming hikes.
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= todayMillis
                override fun isSelectableYear(year: Int) = year >= today.year
            },
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { onDateSelected(utcMillisToLocalDate(it)) }
                        showPicker = false
                    },
                    enabled = pickerState.selectedDateMillis != null,
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DifficultyField(selected: Difficulty?, error: String?, onSelected: (Difficulty) -> Unit) {
    Column {
        SectionTitle("Difficulty")
        Spacer(Modifier.height(8.dp))
        val options = Difficulty.entries
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, difficulty ->
                SegmentedButton(
                    selected = selected == difficulty,
                    onClick = { onSelected(difficulty) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                ) {
                    Text(difficulty.label)
                }
            }
        }
        ErrorText(error)
    }
}

@Composable
private fun ChecklistField(
    state: CreateTripUiState,
    error: String?,
    onToggle: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onNewItemTextChange: (String) -> Unit,
    onAdd: () -> Unit,
) {
    Column {
        SectionTitle("Packing checklist")
        Text(
            "Tick what you want to pack for this trip.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            state.items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = item.included,
                            role = Role.Checkbox,
                            onValueChange = { onToggle(index) },
                        )
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = item.included, onCheckedChange = null)
                    Spacer(Modifier.width(8.dp))
                    Text(item.name, modifier = Modifier.weight(1f))
                    if (item.isCustom) {
                        IconButton(onClick = { onRemove(index) }) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove ${item.name}")
                        }
                    } else {
                        Spacer(Modifier.height(48.dp))
                    }
                }
            }
        }
        ErrorText(error)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.newItemText,
                onValueChange = onNewItemTextChange,
                label = { Text("Add another item") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { onAdd() }),
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onAdd, enabled = state.newItemText.isNotBlank()) {
                Icon(Icons.Filled.Add, contentDescription = "Add item")
            }
        }
    }
}
