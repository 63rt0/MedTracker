package com.berto.medtracker.ui.screens.config

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
fun ConfigScreen(
    onGoToAddEntry: () -> Unit,
    onGoToSeeEntry: () -> Unit
) {
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
            text = "Aquí añadiremos medicamentos, exportación e importación JSON."
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onGoToAddEntry
        ) {
            Text("Ir a Añadir Registro")
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onGoToSeeEntry
        ) {
            Text("Ir a Ver Registros")
        }
    }
}