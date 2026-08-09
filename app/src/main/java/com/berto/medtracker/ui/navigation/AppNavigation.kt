package com.berto.medtracker.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == AppRoutes.ADD,
                    onClick = {
                        navController.navigate(AppRoutes.ADD) {
                            launchSingleTop = true
                            popUpTo(AppRoutes.ADD) {
                                inclusive = false
                            }
                        }
                    },
                    icon = {
                        Text("A")
                    },
                    label = {
                        Text("Añadir")
                    }
                )

                NavigationBarItem(
                    selected = currentRoute == AppRoutes.ENTRIES,
                    onClick = {
                        navController.navigate(AppRoutes.ENTRIES) {
                            launchSingleTop = true
                        }
                    },
                    icon = {
                        Text("R")
                    },
                    label = {
                        Text("Registros")
                    }
                )

                NavigationBarItem(
                    selected = currentRoute == AppRoutes.CONFIG,
                    onClick = {
                        navController.navigate(AppRoutes.CONFIG) {
                            launchSingleTop = true
                        }
                    },
                    icon = {
                        Text("C")
                    },
                    label = {
                        Text("Config")
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = AppRoutes.ADD,
            modifier = Modifier.padding(innerPadding)
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
                    }
                )
            }

            composable(AppRoutes.ENTRIES) {
                EntriesScreen(
                    entryRepository = entryRepository
                )
            }

            composable(AppRoutes.CONFIG) {
                ConfigScreen(
                    entryRepository = entryRepository
                )
            }
        }
    }
}