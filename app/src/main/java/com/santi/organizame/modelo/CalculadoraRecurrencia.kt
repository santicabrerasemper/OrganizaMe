package com.santi.organizame.modelo

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object CalculadoraRecurrencia {

    fun siguienteFecha(
        fechaActual: LocalDate,
        hora: LocalTime?,
        recurrencia: Recurrencia,
        ahora: LocalDateTime = LocalDateTime.now()
    ): LocalDate? {
        if (recurrencia == Recurrencia.SIN_REPETICION) return null

        var candidata = avanzar(fechaActual, recurrencia)

        repeat(MAXIMO_AVANCES) {
            val esFutura = if (hora == null) {
                !candidata.isBefore(ahora.toLocalDate())
            } else {
                LocalDateTime.of(candidata, hora).isAfter(ahora)
            }

            if (esFutura) return candidata
            candidata = avanzar(candidata, recurrencia)
        }

        return null
    }

    private fun avanzar(fecha: LocalDate, recurrencia: Recurrencia): LocalDate =
        when (recurrencia) {
            Recurrencia.SIN_REPETICION -> fecha
            Recurrencia.DIARIA -> fecha.plusDays(1)
            Recurrencia.SEMANAL -> fecha.plusWeeks(1)
            Recurrencia.MENSUAL -> fecha.plusMonths(1)
        }

    private const val MAXIMO_AVANCES = 10_000
}
