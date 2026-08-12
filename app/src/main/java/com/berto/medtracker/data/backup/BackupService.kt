package com.berto.medtracker.data.backup

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.berto.medtracker.data.repository.EntryRepository
import com.berto.medtracker.domain.model.Entry
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class BackupService(
    private val context: Context,
    private val entryRepository: EntryRepository
) {

    private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun exportToJsonFile(): Uri {
        val meds = entryRepository.getAllMeds()
        val entries = entryRepository.getAllEntries()

        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", LocalDateTime.now().format(formatter))

        val medsArray = JSONArray()
        meds.forEach { med ->
            medsArray.put(med)
        }
        root.put("meds", medsArray)

        val entriesArray = JSONArray()
        entries.forEach { entry ->
            val entryObject = JSONObject()
            entryObject.put("med", entry.med)
            entryObject.put("dosis", entry.dosis)
            entryObject.put("efectos", entry.efectos)
            entryObject.put("fechaToma", entry.fechaToma.format(formatter))
            entryObject.put("fechaRegistro", entry.fechaRegistro.format(formatter))
            entryObject.put("info", entry.info)

            entriesArray.put(entryObject)
        }
        root.put("entries", entriesArray)

        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }

        val exportFile = File(exportDir, "medtracker-backup.json")
        exportFile.writeText(root.toString(2))

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            exportFile
        )
    }

    fun importFromJsonUri(uri: Uri) {
        val jsonText = context.contentResolver
            .openInputStream(uri)
            ?.bufferedReader()
            ?.use { reader ->
                reader.readText()
            }
            ?: throw IllegalArgumentException("No se pudo leer el archivo seleccionado")

        val root = JSONObject(jsonText)

        val meds = mutableListOf<String>()
        val entries = mutableListOf<Entry>()

        val medsArray = root.optJSONArray("meds") ?: JSONArray()
        for (index in 0 until medsArray.length()) {
            val med = medsArray.getString(index).trim()

            if (med.isNotBlank()) {
                meds.add(med)
            }
        }

        val entriesArray = root.optJSONArray("entries") ?: JSONArray()
        for (index in 0 until entriesArray.length()) {
            val entryObject = entriesArray.getJSONObject(index)

            val med = entryObject.getString("med").trim()
            val dosis = entryObject.optString("dosis", "").trim()
            val efectos = entryObject.optString("efectos", "").trim()
            val info = entryObject.optString("info", "").trim()

            val fechaToma = LocalDateTime.parse(
                entryObject.getString("fechaToma"),
                formatter
            )

            val fechaRegistro = LocalDateTime.parse(
                entryObject.getString("fechaRegistro"),
                formatter
            )

            if (med.isNotBlank() && dosis.isNotBlank()) {
                entries.add(
                    Entry(
                        med = med,
                        dosis = dosis,
                        efectos = efectos,
                        fechaToma = fechaToma,
                        fechaRegistro = fechaRegistro,
                        info = info
                    )
                )

                if (meds.none { it.equals(med, ignoreCase = true) }) {
                    meds.add(med)
                }
            }
        }

        entryRepository.replaceAllData(
            meds = meds,
            entries = entries
        )
    }
}