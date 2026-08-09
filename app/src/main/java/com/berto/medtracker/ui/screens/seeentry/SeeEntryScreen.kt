package com.berto.medtracker.ui.screens.seeentry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SeeEntryScreen(
    onGoToAddEntry: () -> Unit,
    onGoToConfig: () -> Unit,
    onGoToEditEntry: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Ver Registros",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Aquí mostraremos el listado de registros guardados en la base de datos."
        )

        Button(
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
            onClick = {
                onGoToEditEntry(1)
            }
        ) {
            Text("Probar EditEntry con ID 1")
        }
    }
}