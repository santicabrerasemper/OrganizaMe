package com.santi.organizame

import java.time.LocalDate

data class DiaCalendario(
    val fecha: LocalDate?,
    val tieneActividades: Boolean
)