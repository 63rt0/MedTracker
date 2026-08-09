package com.berto.medtracker.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MedTrackerDbHelper(
    context: Context
) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    override fun onCreate(db: SQLiteDatabase) {
        createEntriesTable(db)
        createMedsTable(db)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        if (oldVersion < 2) {
            createMedsTable(db)
        }
    }

    private fun createEntriesTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                med TEXT NOT NULL,
                dosis TEXT NOT NULL,
                efectos TEXT NOT NULL,
                fecha_toma TEXT NOT NULL,
                fecha_registro TEXT NOT NULL,
                info TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    private fun createMedsTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS meds (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE COLLATE NOCASE
            )
            """.trimIndent()
        )
    }

    companion object {
        private const val DATABASE_NAME = "medtracker.db"
        private const val DATABASE_VERSION = 2
    }
}