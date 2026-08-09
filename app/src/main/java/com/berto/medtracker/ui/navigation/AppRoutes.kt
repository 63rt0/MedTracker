package com.berto.medtracker.ui.navigation

object AppRoutes {
    const val ADD_ENTRY = "add_entry"
    const val SEE_ENTRY = "see_entry"
    const val CONFIG = "config"

    const val EDIT_ENTRY_BASE = "edit_entry"
    const val EDIT_ENTRY = "edit_entry/{entryId}"

    fun editEntry(entryId: Long): String {
        return "$EDIT_ENTRY_BASE/$entryId"
    }
}