package com.berto.medtracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.berto.medtracker.ui.screens.addentry.AddEntryScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface {
                    AddEntryScreen(
                        onAddEntry = { entry ->
                            Log.d(
                                "MedTracker",
                                """
                                Nuevo registro:
                                Med: ${entry.med}
                                Dosis: ${entry.dosis}
                                Efectos: ${entry.efectos}
                                FechaToma: ${entry.fechaToma}
                                FechaRegistro: ${entry.fechaRegistro}
                                Info: ${entry.info}
                                """.trimIndent()
                            )
                        }
                    )
                }
            }
        }
    }
}