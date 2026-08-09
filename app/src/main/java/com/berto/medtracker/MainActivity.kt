package com.berto.medtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.lifecycleScope
import com.berto.medtracker.data.local.MedTrackerDbHelper
import com.berto.medtracker.data.repository.EntryRepository
import com.berto.medtracker.ui.navigation.AppNavigation

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
                    AppNavigation(
                        entryRepository = entryRepository,
                        lifecycleScope = lifecycleScope
                    )
                }
            }
        }
    }
}