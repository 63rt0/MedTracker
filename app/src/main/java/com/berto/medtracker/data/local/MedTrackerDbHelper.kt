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
        db.execSQL(
            """
            CREATE TABLE entries (
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

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        db.execSQL("DROP TABLE IF EXISTS entries")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "medtracker.db"
        private const val DATABASE_VERSION = 1
    }
}