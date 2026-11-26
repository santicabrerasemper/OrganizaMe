package com.santi.organizame.ui.calendario

import java.time.LocalDate

data class DiaCalendario(
    val fecha: LocalDate?,
    val tieneActividades: Boolean
)
