package com.berto.medtracker.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.berto.medtracker.data.repository.EntryRepository
import com.berto.medtracker.ui.screens.addentry.AddEntryScreen
import com.berto.medtracker.ui.screens.config.ConfigScreen
import com.berto.medtracker.ui.screens.entries.EntriesScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    entryRepository: EntryRepository,
    lifecycleScope: LifecycleCoroutineScope
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.ADD
    ) {
        composable(AppRoutes.ADD) {
            AddEntryScreen(
                entryRepository = entryRepository,
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
                },
                onGoToEntries = {
                    navController.navigate(AppRoutes.ENTRIES)
                },
                onGoToConfig = {
                    navController.navigate(AppRoutes.CONFIG)
                }
            )
        }

        composable(AppRoutes.ENTRIES) {
            EntriesScreen(
                entryRepository = entryRepository,
                onGoToAddEntry = {
                    navController.navigate(AppRoutes.ADD)
                },
                onGoToConfig = {
                    navController.navigate(AppRoutes.CONFIG)
                }
            )
        }

        composable(AppRoutes.CONFIG) {
            ConfigScreen(
                entryRepository = entryRepository,
                onGoToAddEntry = {
                    navController.navigate(AppRoutes.ADD)
                },
                onGoToEntries = {
                    navController.navigate(AppRoutes.ENTRIES)
                }
            )
        }
    }
}