package com.berto.medtracker.ui.screens.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.berto.medtracker.data.repository.EntryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    entryRepository: EntryRepository,
    onGoToAddEntry: () -> Unit,
    onGoToEntries: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var newMed by remember { mutableStateOf("") }
    var meds by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedMed by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var newMedError by remember { mutableStateOf(false) }

    suspend fun refreshMeds() {
        meds = withContext(Dispatchers.IO) {
            entryRepository.getAllMeds()
        }

        if (selectedMed.isBlank() || selectedMed !in meds) {
            selectedMed = meds.firstOrNull().orEmpty()
        }
    }

    LaunchedEffect(Unit) {
        refreshMeds()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Config",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Gestión de medicamentos disponibles para la app.",
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            value = newMed,
            onValueChange = {
                newMed = it
                newMedError = false
                message = ""
            },
            label = { Text("Añadir un Med") },
            placeholder = { Text("Ejemplo: Paracetamol") },
            isError = newMedError,
            supportingText = {
                if (newMedError) {
                    Text("Introduce un nombre válido")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val medName = newMed.trim()

                if (medName.isBlank()) {
                    newMedError = true
                    message = "No se puede añadir un Med vacío."
                    return@Button
                }

                coroutineScope.launch {
                    val added = withContext(Dispatchers.IO) {
                        entryRepository.addMed(medName)
                    }

                    refreshMeds()

                    if (added) {
                        selectedMed = medName
                        newMed = ""
                        message = "Med añadido."
                    } else {
                        message = "Ese Med ya existe. No se ha añadido de nuevo."
                    }
                }
            }
        ) {
            Text("Añadir Med")
        }

        Text(
            text = "Borrar un Med",
            style = MaterialTheme.typography.titleMedium
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
                            message = ""
                        }
                    )
                }
            }
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedMed.isNotBlank(),
            onClick = {
                val medToDelete = selectedMed.trim()

                if (medToDelete.isBlank()) {
                    message = "No hay ningún Med seleccionado."
                    return@OutlinedButton
                }

                coroutineScope.launch {
                    val deleted = withContext(Dispatchers.IO) {
                        entryRepository.deleteMed(medToDelete)
                    }

                    refreshMeds()

                    message = if (deleted) {
                        "Med borrado."
                    } else {
                        "No se puede borrar este Med porque existe en algún registro."
                    }
                }
            }
        ) {
            Text("Borrar Med")
        }

        if (message.isNotBlank()) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onGoToAddEntry
        ) {
            Text("Añadir")
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onGoToEntries
        ) {
            Text("Registros")
        }
    }
}