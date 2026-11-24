package com.santi.organizame.modelo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class CalculadoraRecurrenciaTest {

    @Test
    fun sinRepeticionNoGeneraOtraFecha() {
        assertNull(
            CalculadoraRecurrencia.siguienteFecha(
                LocalDate.of(2026, 11, 23),
                null,
                Recurrencia.SIN_REPETICION
            )
        )
    }

    @Test
    fun repeticionDiariaSaltaOcurrenciasPasadas() {
        val siguiente = CalculadoraRecurrencia.siguienteFecha(
            fechaActual = LocalDate.of(2026, 11, 20),
            hora = LocalTime.of(18, 0),
            recurrencia = Recurrencia.DIARIA,
            ahora = LocalDateTime.of(2026, 11, 23, 19, 0)
        )

        assertEquals(LocalDate.of(2026, 11, 24), siguiente)
    }

    @Test
    fun repeticionSemanalConservaElDia() {
        val siguiente = CalculadoraRecurrencia.siguienteFecha(
            fechaActual = LocalDate.of(2026, 11, 23),
            hora = null,
            recurrencia = Recurrencia.SEMANAL,
            ahora = LocalDateTime.of(2026, 11, 23, 10, 0)
        )

        assertEquals(LocalDate.of(2026, 11, 30), siguiente)
    }

    @Test
    fun repeticionMensualAjustaElFinDeMes() {
        val siguiente = CalculadoraRecurrencia.siguienteFecha(
            fechaActual = LocalDate.of(2027, 1, 31),
            hora = null,
            recurrencia = Recurrencia.MENSUAL,
            ahora = LocalDateTime.of(2027, 1, 31, 10, 0)
        )

        assertEquals(LocalDate.of(2027, 2, 28), siguiente)
    }
}
