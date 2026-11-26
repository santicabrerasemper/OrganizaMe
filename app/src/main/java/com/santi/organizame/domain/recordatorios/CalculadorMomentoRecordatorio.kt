package com.santi.organizame.domain.recordatorios

import com.santi.organizame.modelo.AnticipacionRecordatorio
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object CalculadorMomentoRecordatorio {
    fun calcular(
        fecha: LocalDate?,
        hora: LocalTime?,
        recordatorio: AnticipacionRecordatorio?,
        zona: ZoneId = ZoneId.systemDefault()
    ): Instant? {
        if (fecha == null || hora == null || recordatorio == null) return null

        val minutosAntes = when (recordatorio) {
            AnticipacionRecordatorio.A_LA_HORA -> 0L
            AnticipacionRecordatorio.DIEZ_MINUTOS_ANTES -> 10L
            AnticipacionRecordatorio.TREINTA_MINUTOS_ANTES -> 30L
            AnticipacionRecordatorio.UNA_HORA_ANTES -> 60L
            AnticipacionRecordatorio.UN_DIA_ANTES -> 1440L
        }

        return LocalDateTime.of(fecha, hora)
            .minusMinutes(minutosAntes)
            .atZone(zona)
            .toInstant()
    }
}
