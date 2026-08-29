package com.berto.medtracker.ui.screens.config

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.berto.medtracker.data.backup.BackupService
import com.berto.medtracker.data.repository.EntryRepository
import com.berto.medtracker.ui.components.ScreenHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    entryRepository: EntryRepository
) {
    val coroutineScope = rememberCoroutineScope()

    var newMed by remember { mutableStateOf("") }
    var meds by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedMed by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var newMedError by remember { mutableStateOf(false) }
    var showImportConfirmation by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    val backupService = remember {
        BackupService(
            context = context,
            entryRepository = entryRepository
        )
    }

    suspend fun refreshMeds() {
        meds = withContext(Dispatchers.IO) {
            entryRepository.getAllMeds()
        }

        if (selectedMed.isBlank() || selectedMed !in meds) {
            selectedMed = meds.firstOrNull().orEmpty()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                coroutineScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            backupService.importFromJsonUri(uri)
                        }

                        refreshMeds()

                        message =
                            "Importación completada. Se han sustituido los datos actuales."
                    } catch (_: Exception) {
                        message = "No se pudo importar el archivo."
                    }
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        refreshMeds()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        ScreenHeader(
            title = "Config",
            subtitle = "Meds y copias de seguridad"
        )

        // =====================================================
        // AÑADIR MED
        // =====================================================

        Text(
            text = "Añadir Med",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = newMed,
            onValueChange = {
                newMed = it
                newMedError = false
                message = ""
            },
            label = {
                Text("Nuevo Med")
            },
            placeholder = {
                Text("Ejemplo: Paracetamol")
            },
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
                        message =
                            "Ese Med ya existe. No se ha añadido de nuevo."
                    }
                }
            }
        ) {
            Text("Añadir Med")
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            thickness = 1.dp
        )

        // =====================================================
        // BORRAR MED
        // =====================================================

        Text(
            text = "Borrar Med",
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
                label = {
                    Text("Med")
                },
                placeholder = {
                    Text("No hay Meds disponibles")
                },
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

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            thickness = 1.dp
        )

        // =====================================================
        // BASE DE DATOS
        // =====================================================

        Text(
            text = "Base de datos",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = {

                    coroutineScope.launch {

                        try {

                            val uri = withContext(Dispatchers.IO) {
                                backupService.exportToJsonFile()
                            }

                            val shareIntent =
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(
                                        Intent.EXTRA_STREAM,
                                        uri
                                    )
                                    addFlags(
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    )
                                }

                            context.startActivity(
                                Intent.createChooser(
                                    shareIntent,
                                    "Exportar BBDD"
                                )
                            )

                            message =
                                "Archivo de exportación preparado."

                        } catch (_: Exception) {

                            message =
                                "No se pudo exportar la BBDD."
                        }
                    }
                }
            ) {
                Text("Exportar")
            }

            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = {
                    showImportConfirmation = true
                }
            ) {
                Text("Importar")
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
    if (showImportConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showImportConfirmation = false
            },
            title = {
                Text("Importar base de datos")
            },
            text = {
                Text(
                    "La importación sustituirá todos los medicamentos " +
                            "y registros actuales. Esta acción no se puede deshacer. " +
                            "Es recomendable exportar una copia antes de continuar."
                )
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showImportConfirmation = false
                    }
                ) {
                    Text("Cancelar")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showImportConfirmation = false

                        importLauncher.launch(
                            arrayOf(
                                "application/json",
                                "text/plain",
                                "*/*"
                            )
                        )
                    }
                ) {
                    Text("Continuar")
                }
            }
        )
    }
}