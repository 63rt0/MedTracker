package com.berto.medtracker.ui.screens.entries

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import com.berto.medtracker.ui.components.ScreenHeader
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
fun EntriesScreen(
    entryRepository: EntryRepository
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var meds by remember { mutableStateOf<List<String>>(emptyList()) }
    var entries by remember { mutableStateOf<List<Entry>>(emptyList()) }

    var selectedMed by remember { mutableStateOf("") }
    var medDropdownExpanded by remember { mutableStateOf(false) }

    var dateFrom by remember {
        mutableStateOf(LocalDate.now().minusMonths(1))
    }

    var dateTo by remember {
        mutableStateOf(LocalDate.now())
    }

    var message by remember { mutableStateOf("") }
    var entryBeingEdited by remember { mutableStateOf<Entry?>(null) }

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

                message = ""
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

                message = ""
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
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ScreenHeader(
            title = "Registros",
            subtitle = "Ver y editar registros"
        )

        ExposedDropdownMenuBox(
            expanded = medDropdownExpanded,
            onExpandedChange = {
                medDropdownExpanded = !medDropdownExpanded
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
                        expanded = medDropdownExpanded
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = medDropdownExpanded,
                onDismissRequest = {
                    medDropdownExpanded = false
                }
            ) {
                meds.forEach { med ->
                    DropdownMenuItem(
                        text = {
                            Text(med)
                        },
                        onClick = {
                            selectedMed = med
                            medDropdownExpanded = false
                            message = ""
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
                    text = "Fecha más reciente: ${
                        mostRecentFechaToma.format(seeEntryDateTimeFormatter)
                    }",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = averageDaysText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Text(
            text = "Filtro de fechas",
            style = MaterialTheme.typography.titleMedium
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

        Text(
            text = "Registros",
            style = MaterialTheme.typography.titleMedium
        )

        if (selectedMed.isBlank()) {
            Text(
                text = "Selecciona una Med para ver sus registros.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else if (dateFrom.isAfter(dateTo)) {
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
                style = MaterialTheme.typography.bodyMedium
            )

            filteredEntries.forEach { entry ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            entryBeingEdited = entry
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Fecha: ${
                                entry.fechaToma.format(seeEntryDateTimeFormatter)
                            }",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "Dosis: ${entry.dosis}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "Info: ${
                                entry.info.ifBlank { "Sin info" }
                            }",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        if (message.isNotBlank()) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    val editableEntry = entryBeingEdited

    if (editableEntry != null) {
        EditEntryPopup(
            entry = editableEntry,
            meds = meds,
            entryRepository = entryRepository,
            onDismiss = {
                entryBeingEdited = null
            },
            onEntryDeleted = {
                coroutineScope.launch {
                    refreshData()
                    entryBeingEdited = null
                    message = "Registro borrado."
                }
            },
            onEntryUpdated = {
                coroutineScope.launch {
                    refreshData()
                    entryBeingEdited = null
                    message = "Registro editado."
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditEntryPopup(
    entry: Entry,
    meds: List<String>,
    entryRepository: EntryRepository,
    onDismiss: () -> Unit,
    onEntryDeleted: () -> Unit,
    onEntryUpdated: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedMed by remember(entry.id) {
        mutableStateOf(entry.med)
    }

    var medDropdownExpanded by remember { mutableStateOf(false) }

    var dosis by remember(entry.id) {
        mutableStateOf(entry.dosis)
    }

    var efectos by remember { entry.id
        mutableStateOf(entry.efectos)
    }

    var info by remember(entry.id) {
        mutableStateOf(entry.info)
    }

    var selectedDate by remember(entry.id) {
        mutableStateOf(entry.fechaToma.toLocalDate())
    }

    var selectedTime by remember(entry.id) {
        mutableStateOf(entry.fechaToma.toLocalTime().withSecond(0).withNano(0))
    }

    var localMessage by remember { mutableStateOf("") }

    var showDeleteConfirmation by remember {
        mutableStateOf(false)
    }

    val selectedDateTime = LocalDateTime.of(selectedDate, selectedTime)

    fun openTimePicker() {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                selectedTime = LocalTime.of(hourOfDay, minute)
            },
            selectedTime.hour,
            selectedTime.minute,
            true
        ).show()
    }

    fun openDatePicker() {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedDate = LocalDate.of(
                    year,
                    month + 1,
                    dayOfMonth
                )

                openTimePicker()
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Editar registro")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = medDropdownExpanded,
                    onExpandedChange = {
                        medDropdownExpanded = !medDropdownExpanded
                    }
                ) {
                    OutlinedTextField(
                        value = selectedMed,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Med") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = medDropdownExpanded
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = medDropdownExpanded,
                        onDismissRequest = {
                            medDropdownExpanded = false
                        }
                    ) {
                        meds.forEach { med ->
                            DropdownMenuItem(
                                text = {
                                    Text(med)
                                },
                                onClick = {
                                    selectedMed = med
                                    medDropdownExpanded = false
                                    localMessage = ""
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = efectos,
                    onValueChange = {
                        efectos = it
                        localMessage = ""
                    },
                    label = { Text("Efectos") },
                    placeholder = {
                        Text("Efectos observados")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Fecha: ${
                            selectedDateTime.format(seeEntryDateTimeFormatter)
                        }",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            openDatePicker()
                        }
                    ) {
                        Text("Seleccionar fecha y hora")
                    }
                }

                OutlinedTextField(
                    value = info,
                    onValueChange = {
                        info = it
                    },
                    label = { Text("Info") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                if (localMessage.isNotBlank()) {
                    Text(
                        text = localMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss
                ) {
                    Text("Salir")
                }

                OutlinedButton(
                    onClick = {
                        showDeleteConfirmation = true
                    }
                ) {
                    Text("Borrar")
                }

                OutlinedButton(
                    onClick = {
                        val cleanMed = selectedMed.trim()
                        val cleanDosis = dosis.trim()

                        if (cleanMed.isBlank()) {
                            localMessage = "Debes seleccionar una Med."
                            return@OutlinedButton
                        }

                        if (cleanDosis.isBlank()) {
                            localMessage = "La dosis no puede estar vacía."
                            return@OutlinedButton
                        }

                        val editedEntry = entry.copy(
                            med = cleanMed,
                            dosis = cleanDosis,
                            efectos = efectos.trim(),
                            fechaToma = LocalDateTime.of(
                                selectedDate,
                                selectedTime
                            ),
                            info = info.trim()
                        )

                        coroutineScope.launch {
                            val updated = withContext(Dispatchers.IO) {
                                entryRepository.updateEntry(editedEntry)
                            }

                            if (updated) {
                                onEntryUpdated()
                            } else {
                                localMessage = "No se pudo editar el registro."
                            }
                        }
                    }
                ) {
                    Text("Editar")
                }
            }
        }
    )
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmation = false
            },
            title = {
                Text("Borrar registro")
            },
            text = {
                Text(
                    "¿Seguro que quieres borrar este registro? " +
                            "Esta acción no se puede deshacer."
                )
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Cancelar")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false

                        coroutineScope.launch {
                            val deleted = withContext(Dispatchers.IO) {
                                entryRepository.deleteEntry(entry.id)
                            }

                            if (deleted) {
                                onEntryDeleted()
                            } else {
                                localMessage =
                                    "No se pudo borrar el registro."
                            }
                        }
                    }
                ) {
                    Text("Borrar")
                }
            }
        )
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