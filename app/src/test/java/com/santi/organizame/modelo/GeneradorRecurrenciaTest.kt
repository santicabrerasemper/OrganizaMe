package com.santi.organizame.modelo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

class GeneradorRecurrenciaTest {

    @Test
    fun creaUnaNuevaTareaPendienteSinModificarLaCompletada() {
        val completada = Actividad(
            id = 8,
            tipo = TipoActividad.TAREA,
            titulo = "Revisar agenda",
            fecha = LocalDate.of(2026, 11, 23),
            estadoTarea = EstadoTarea.COMPLETADA,
            fechaCompletada = Instant.parse("2026-11-23T15:00:00Z"),
            recurrencia = Recurrencia.SEMANAL,
            recurrenciaProcesada = true
        )
        val momento = Instant.parse("2026-11-23T15:00:00Z")

        val siguiente = GeneradorRecurrencia.crearSiguiente(
            completada,
            momento,
            LocalDateTime.of(2026, 11, 23, 12, 0)
        )

        checkNotNull(siguiente)
        assertEquals(0L, siguiente.id)
        assertEquals(LocalDate.of(2026, 11, 30), siguiente.fecha)
        assertEquals(EstadoTarea.PENDIENTE, siguiente.estadoTarea)
        assertNull(siguiente.fechaCompletada)
        assertFalse(siguiente.recurrenciaProcesada)
        assertEquals(EstadoTarea.COMPLETADA, completada.estadoTarea)
    }

    @Test
    fun noCreaSiguienteParaUnaTareaSinRepeticion() {
        assertNull(
            GeneradorRecurrencia.crearSiguiente(
                Actividad(
                    tipo = TipoActividad.TAREA,
                    titulo = "Única",
                    fecha = LocalDate.of(2026, 11, 23),
                    recurrencia = Recurrencia.SIN_REPETICION
                )
            )
        )
    }
}
