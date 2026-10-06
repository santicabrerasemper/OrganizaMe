package com.santi.organizame.modelo

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class ConversoresRoomTest {

    private val conversores = ConversoresRoom()

    @Test
    fun convierteFechaSinPerderInformacion() {
        val fecha = LocalDate.of(2026, 10, 6)

        assertEquals(fecha, conversores.valorALocalDate(conversores.localDateAValor(fecha)))
    }

    @Test
    fun convierteHoraSinPerderInformacion() {
        val hora = LocalTime.of(18, 30)

        assertEquals(hora, conversores.valorALocalTime(conversores.localTimeAValor(hora)))
    }

    @Test
    fun convierteInstanteSinPerderInformacion() {
        val instante = Instant.parse("2026-10-06T21:30:00Z")

        assertEquals(instante, conversores.valorAInstant(conversores.instantAValor(instante)))
    }

    @Test
    fun conviertePalabrasClaveSinPerderElOrden() {
        val palabras = listOf("facultad", "estudio", "parcial")

        assertEquals(
            palabras,
            conversores.valorAPalabrasClave(conversores.palabrasClaveAValor(palabras))
        )
    }
}
