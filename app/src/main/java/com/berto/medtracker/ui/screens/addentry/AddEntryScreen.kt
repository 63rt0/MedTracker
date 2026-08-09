package com.berto.medtracker.ui.screens.addentry

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val displayDateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(
    entryRepository: EntryRepository,
    onAddEntry: (Entry) -> Unit,
    onGoToSeeEntry: () -> Unit,
    onGoToConfig: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var meds by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedMed by remember { mutableStateOf("") }
    var medDropdownExpanded by remember { mutableStateOf(false) }

    var dosis by remember { mutableStateOf("") }
    var efectos by remember { mutableStateOf("") }
    var info by remember { mutableStateOf("") }

    val initialDateTime = remember {
        LocalDateTime.now().withSecond(0).withNano(0)
    }

    var selectedDate by remember {
        mutableStateOf(initialDateTime.toLocalDate())
    }

    var selectedTime by remember {
        mutableStateOf(initialDateTime.toLocalTime())
    }

    var medError by remember { mutableStateOf(false) }
    var dosisError by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    val selectedDateTime = LocalDateTime.of(selectedDate, selectedTime)

    suspend fun refreshMeds() {
        val loadedMeds = withContext(Dispatchers.IO) {
            entryRepository.getAllMeds()
        }

        meds = loadedMeds

        if (selectedMed.isBlank() || selectedMed !in loadedMeds) {
            selectedMed = loadedMeds.firstOrNull().orEmpty()
        }

        if (selectedMed.isNotBlank()) {
            medError = false
        }
    }

    LaunchedEffect(Unit) {
        refreshMeds()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                coroutineScope.launch {
                    refreshMeds()
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Añadir Registro",
            style = MaterialTheme.typography.headlineMedium
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
                placeholder = { Text("No hay Meds. Añade uno en Config.") },
                isError = medError,
                supportingText = {
                    if (medError) {
                        Text("Debes seleccionar un Med")
                    }
                },
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
                            medError = false
                            message = ""
                            medDropdownExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = dosis,
            onValueChange = {
                dosis = it
                dosisError = false
            },
            label = { Text("Dosis") },
            placeholder = { Text("Ejemplo: 500 mg") },
            isError = dosisError,
            supportingText = {
                if (dosisError) {
                    Text("Dosis es obligatoria")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = efectos,
            onValueChange = {
                efectos = it
            },
            label = { Text("Efectos") },
            placeholder = { Text("Ejemplo: Dolor reducido") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Fecha",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = selectedDateTime.format(displayDateTimeFormatter),
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
            placeholder = { Text("Información adicional") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val medValue = selectedMed.trim()
                val dosisValue = dosis.trim()

                medError = medValue.isBlank()
                dosisError = dosisValue.isBlank()
                message = ""

                if (medError || dosisError) {
                    message = "Revisa los campos obligatorios."
                    return@Button
                }

                val fechaRegistro = LocalDateTime.now()
                val fechaToma = LocalDateTime.of(selectedDate, selectedTime)

                val entry = Entry(
                    med = medValue,
                    dosis = dosisValue,
                    efectos = efectos.trim(),
                    fechaToma = fechaToma,
                    fechaRegistro = fechaRegistro,
                    info = info.trim()
                )

                onAddEntry(entry)

                dosis = ""
                efectos = ""
                info = ""

                val now = LocalDateTime.now().withSecond(0).withNano(0)
                selectedDate = now.toLocalDate()
                selectedTime = now.toLocalTime()

                message = "Registro añadido."
            }
        ) {
            Text("Añadir Registro")
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onGoToSeeEntry
        ) {
            Text("Ver Registros")
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onGoToConfig
        ) {
            Text("Config")
        }

        if (message.isNotBlank()) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}