package com.berto.medtracker.data.repository

import android.content.ContentValues
import com.berto.medtracker.data.local.MedTrackerDbHelper
import com.berto.medtracker.domain.model.Entry
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class EntryRepository(
    private val dbHelper: MedTrackerDbHelper
) {

    private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun insertEntry(entry: Entry): Long {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put("med", entry.med)
            put("dosis", entry.dosis)
            put("efectos", entry.efectos)
            put("fecha_toma", entry.fechaToma.format(formatter))
            put("fecha_registro", entry.fechaRegistro.format(formatter))
            put("info", entry.info)
        }

        return db.insert("entries", null, values)
    }

    fun getAllEntries(): List<Entry> {
        val db = dbHelper.readableDatabase

        val cursor = db.query(
            "entries",
            null,
            null,
            null,
            null,
            null,
            "fecha_toma DESC"
        )

        val entries = mutableListOf<Entry>()

        cursor.use {
            while (it.moveToNext()) {
                val entry = Entry(
                    id = it.getLong(it.getColumnIndexOrThrow("id")),
                    med = it.getString(it.getColumnIndexOrThrow("med")),
                    dosis = it.getString(it.getColumnIndexOrThrow("dosis")),
                    efectos = it.getString(it.getColumnIndexOrThrow("efectos")),
                    fechaToma = LocalDateTime.parse(
                        it.getString(it.getColumnIndexOrThrow("fecha_toma")),
                        formatter
                    ),
                    fechaRegistro = LocalDateTime.parse(
                        it.getString(it.getColumnIndexOrThrow("fecha_registro")),
                        formatter
                    ),
                    info = it.getString(it.getColumnIndexOrThrow("info"))
                )

                entries.add(entry)
            }
        }

        return entries
    }

    fun deleteAllEntries() {
        val db = dbHelper.writableDatabase
        db.delete("entries", null, null)
    }
}