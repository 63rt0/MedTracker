package com.berto.medtracker.data.repository

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
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

    fun addMed(name: String): Boolean {
        val cleanName = name.trim()

        if (cleanName.isBlank()) {
            return false
        }

        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put("name", cleanName)
        }

        val result = db.insertWithOnConflict(
            "meds",
            null,
            values,
            SQLiteDatabase.CONFLICT_IGNORE
        )

        return result != -1L
    }

    fun getAllMeds(): List<String> {
        val db = dbHelper.readableDatabase

        val cursor = db.query(
            "meds",
            arrayOf("name"),
            null,
            null,
            null,
            null,
            "name COLLATE NOCASE ASC"
        )

        val meds = mutableListOf<String>()

        cursor.use {
            while (it.moveToNext()) {
                meds.add(
                    it.getString(it.getColumnIndexOrThrow("name"))
                )
            }
        }

        return meds
    }

    fun canDeleteMed(name: String): Boolean {
        val cleanName = name.trim()

        if (cleanName.isBlank()) {
            return false
        }

        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT COUNT(*) AS total
            FROM entries
            WHERE med = ? COLLATE NOCASE
            """.trimIndent(),
            arrayOf(cleanName)
        )

        cursor.use {
            return if (it.moveToFirst()) {
                val total = it.getInt(it.getColumnIndexOrThrow("total"))
                total == 0
            } else {
                false
            }
        }
    }

    fun deleteMed(name: String): Boolean {
        val cleanName = name.trim()

        if (!canDeleteMed(cleanName)) {
            return false
        }

        val db = dbHelper.writableDatabase

        val deletedRows = db.delete(
            "meds",
            "name = ? COLLATE NOCASE",
            arrayOf(cleanName)
        )

        return deletedRows > 0
    }

    fun updateEntry(entry: Entry): Boolean {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put("med", entry.med)
            put("dosis", entry.dosis)
            put("efectos", entry.efectos)
            put("fecha_toma", entry.fechaToma.format(formatter))
            put("fecha_registro", entry.fechaRegistro.format(formatter))
            put("info", entry.info)
        }

        val updatedRows = db.update(
            "entries",
            values,
            "id = ?",
            arrayOf(entry.id.toString())
        )

        return updatedRows > 0
    }

    fun deleteEntry(entryId: Long): Boolean {
        val db = dbHelper.writableDatabase

        val deletedRows = db.delete(
            "entries",
            "id = ?",
            arrayOf(entryId.toString())
        )

        return deletedRows > 0
    }
}