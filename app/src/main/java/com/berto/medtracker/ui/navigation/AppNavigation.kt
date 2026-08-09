package com.berto.medtracker.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.berto.medtracker.data.repository.EntryRepository
import com.berto.medtracker.ui.screens.addentry.AddEntryScreen
import com.berto.medtracker.ui.screens.config.ConfigScreen
import com.berto.medtracker.ui.screens.editentry.EditEntryScreen
import com.berto.medtracker.ui.screens.seeentry.SeeEntryScreen
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
        startDestination = AppRoutes.ADD_ENTRY
    ) {
        composable(AppRoutes.ADD_ENTRY) {
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
                onGoToSeeEntry = {
                    navController.navigate(AppRoutes.SEE_ENTRY)
                },
                onGoToConfig = {
                    navController.navigate(AppRoutes.CONFIG)
                }
            )
        }

        composable(AppRoutes.SEE_ENTRY) {
            SeeEntryScreen(
                onGoToAddEntry = {
                    navController.navigate(AppRoutes.ADD_ENTRY)
                },
                onGoToConfig = {
                    navController.navigate(AppRoutes.CONFIG)
                },
                onGoToEditEntry = { entryId ->
                    navController.navigate(AppRoutes.editEntry(entryId))
                }
            )
        }

        composable(
            route = AppRoutes.EDIT_ENTRY,
            arguments = listOf(
                navArgument("entryId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: 0L

            EditEntryScreen(
                entryId = entryId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoutes.CONFIG) {
            ConfigScreen(
                entryRepository = entryRepository,
                onGoToAddEntry = {
                    navController.navigate(AppRoutes.ADD_ENTRY)
                },
                onGoToSeeEntry = {
                    navController.navigate(AppRoutes.SEE_ENTRY)
                }
            )
        }
    }
}