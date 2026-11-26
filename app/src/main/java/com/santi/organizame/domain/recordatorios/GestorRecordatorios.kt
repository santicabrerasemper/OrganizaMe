package com.santi.organizame.domain.recordatorios

import com.santi.organizame.modelo.Actividad

interface GestorRecordatorios {
    fun programar(actividad: Actividad): ResultadoProgramacionRecordatorio
    fun cancelar(actividadId: Long)
}

enum class ResultadoProgramacionRecordatorio {
    EXACTO,
    APROXIMADO,
    NO_PROGRAMADO
}
