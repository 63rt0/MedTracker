package com.berto.medtracker.domain.model

import java.time.LocalDateTime

data class Entry(
    val id: Long = 0,
    val med: String,
    val dosis: String,
    val efectos: String,
    val fechaToma: LocalDateTime,
    val fechaRegistro: LocalDateTime,
    val info: String
)