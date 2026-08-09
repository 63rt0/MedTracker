package com.berto.medtracker.ui.screens.seeentry

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.berto.medtracker.data.repository.EntryRepository
import com.berto.medtracker.domain.model.Entry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val seeEntryDateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

private val seeEntryDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeeEntryScreen(
    entryRepository: EntryRepository,
    onGoToAddEntry: () -> Unit,
    onGoToConfig: () -> Unit,
    onGoToEditEntry: (Long) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var meds by remember { mutableStateOf<List<String>>(emptyList()) }
    var entries by remember { mutableStateOf<List<Entry>>(emptyList()) }

    var selectedMed by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    var dateFrom by remember {
        mutableStateOf(LocalDate.now().minusMonths(1))
    }

    var dateTo by remember {
        mutableStateOf(LocalDate.now())
    }

    var showFilteredEntries by remember { mutableStateOf(false) }

    suspend fun refreshData() {
        val loadedMeds = withContext(Dispatchers.IO) {
            entryRepository.getAllMeds()
        }

        val loadedEntries = withContext(Dispatchers.IO) {
            entryRepository.getAllEntries()
        }

        meds = loadedMeds
        entries = loadedEntries

        if (selectedMed.isBlank() || selectedMed !in loadedMeds) {
            selectedMed = loadedMeds.firstOrNull().orEmpty()
        }
    }

    LaunchedEffect(Unit) {
        refreshData()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                coroutineScope.launch {
                    refreshData()
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    fun openDateFromPicker() {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                dateFrom = LocalDate.of(
                    year,
                    month + 1,
                    dayOfMonth
                )

                showFilteredEntries = false
            },
            dateFrom.year,
            dateFrom.monthValue - 1,
            dateFrom.dayOfMonth
        ).show()
    }

    fun openDateToPicker() {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                dateTo = LocalDate.of(
                    year,
                    month + 1,
                    dayOfMonth
                )

                showFilteredEntries = false
            },
            dateTo.year,
            dateTo.monthValue - 1,
            dateTo.dayOfMonth
        ).show()
    }

    val selectedEntries = entries
        .filter { entry ->
            entry.med.equals(selectedMed, ignoreCase = true)
        }
        .sortedByDescending { entry ->
            entry.fechaToma
        }

    val mostRecentFechaToma = selectedEntries.firstOrNull()?.fechaToma

    val averageDaysText = calculateAverageDaysText(
        med = selectedMed,
        entries = selectedEntries
    )

    val fromDateTime = LocalDateTime.of(dateFrom, LocalTime.MIN)
    val toDateTime = LocalDateTime.of(dateTo, LocalTime.MAX)

    val filteredEntries = selectedEntries
        .filter { entry ->
            !entry.fechaToma.isBefore(fromDateTime) &&
                    !entry.fechaToma.isAfter(toDateTime)
        }
        .sortedByDescending { entry ->
            entry.fechaToma
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Ver Registros",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Selecciona una Med para ver su última toma y su frecuencia aproximada.",
            style = MaterialTheme.typography.bodyMedium
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {
            OutlinedTextField(
                value = selectedMed,
                onValueChange = {},
                readOnly = true,
                label = { Text("Med") },
                placeholder = { Text("No hay Meds disponibles") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {
                meds.forEach { med ->
                    DropdownMenuItem(
                        text = {
                            Text(med)
                        },
                        onClick = {
                            selectedMed = med
                            expanded = false
                            showFilteredEntries = false
                        }
                    )
                }
            }
        }

        if (selectedMed.isBlank()) {
            Text(
                text = "No hay ninguna Med creada. Añade una desde Config.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Text(
                text = "Med seleccionada: $selectedMed",
                style = MaterialTheme.typography.titleMedium
            )

            if (mostRecentFechaToma == null) {
                Text(
                    text = "Fecha más reciente: sin registros para esta Med.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Se toma $selectedMed: sin datos suficientes.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = "Fecha más reciente: ${mostRecentFechaToma.format(seeEntryDateTimeFormatter)}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = averageDaysText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Text(
            text = "Ver registros",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                openDateFromPicker()
            }
        ) {
            Text("Desde: ${dateFrom.format(seeEntryDateFormatter)}")
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                openDateToPicker()
            }
        ) {
            Text("Hasta: ${dateTo.format(seeEntryDateFormatter)}")
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedMed.isNotBlank(),
            onClick = {
                showFilteredEntries = true
            }
        ) {
            Text("Ver registros")
        }

        if (showFilteredEntries) {
            if (dateFrom.isAfter(dateTo)) {
                Text(
                    text = "La fecha Desde no puede ser posterior a la fecha Hasta.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else if (filteredEntries.isEmpty()) {
                Text(
                    text = "No hay registros para $selectedMed entre las fechas seleccionadas.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = "Registros encontrados: ${filteredEntries.size}",
                    style = MaterialTheme.typography.titleMedium
                )

                filteredEntries.forEach { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Fecha: ${entry.fechaToma.format(seeEntryDateTimeFormatter)}",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = "Dosis: ${entry.dosis}",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = "Info: ${entry.info.ifBlank { "Sin info" }}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onGoToAddEntry
        ) {
            Text("Ir a Añadir Registro")
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onGoToConfig
        ) {
            Text("Ir a Config")
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedEntries.firstOrNull() != null,
            onClick = {
                val lastEntryId = selectedEntries.firstOrNull()?.id

                if (lastEntryId != null) {
                    onGoToEditEntry(lastEntryId)
                }
            }
        ) {
            Text("Editar último registro")
        }
    }
}

private fun calculateAverageDaysText(
    med: String,
    entries: List<Entry>
): String {
    if (med.isBlank()) {
        return "Se toma: sin Med seleccionada."
    }

    if (entries.size < 2) {
        return "Se toma $med: sin datos suficientes."
    }

    val sortedEntries = entries.sortedBy { entry ->
        entry.fechaToma
    }

    val intervalsInMinutes = sortedEntries
        .zipWithNext()
        .map { pair ->
            val firstDate: LocalDateTime = pair.first.fechaToma
            val secondDate: LocalDateTime = pair.second.fechaToma

            Duration.between(firstDate, secondDate).toMinutes()
        }
        .filter { minutes ->
            minutes > 0
        }

    if (intervalsInMinutes.isEmpty()) {
        return "Se toma $med: sin datos suficientes."
    }

    val averageMinutes = intervalsInMinutes.average()
    val averageDays = averageMinutes / 1440.0

    val cleanAverageDays = roundToOneDecimal(averageDays)

    return if (cleanAverageDays == 1.0) {
        "Se toma $med cada 1 día."
    } else {
        "Se toma $med cada $cleanAverageDays días."
    }
}

private fun roundToOneDecimal(value: Double): Double {
    return (value * 10.0).roundToInt() / 10.0
}