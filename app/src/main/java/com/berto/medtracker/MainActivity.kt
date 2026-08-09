package com.berto.medtracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.lifecycleScope
import com.berto.medtracker.data.local.MedTrackerDbHelper
import com.berto.medtracker.data.repository.EntryRepository
import com.berto.medtracker.ui.screens.addentry.AddEntryScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var entryRepository: EntryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        entryRepository = EntryRepository(
            MedTrackerDbHelper(applicationContext)
        )

        setContent {
            MaterialTheme {
                Surface {
                    AddEntryScreen(
                        onAddEntry = { entry ->
                            lifecycleScope.launch(Dispatchers.IO) {
                                val insertedId = entryRepository.insertEntry(entry)

                                Log.d(
                                    "MedTracker",
                                    """
                                    Registro guardado en BBDD:
                                    ID: $insertedId
                                    Med: ${entry.med}
                                    Dosis: ${entry.dosis}
                                    Efectos: ${entry.efectos}
                                    FechaToma: ${entry.fechaToma}
                                    FechaRegistro: ${entry.fechaRegistro}
                                    Info: ${entry.info}
                                    """.trimIndent()
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}