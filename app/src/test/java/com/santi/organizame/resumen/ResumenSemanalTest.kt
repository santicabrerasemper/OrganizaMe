package com.santi.organizame.resumen

import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.EstadoTarea
import com.santi.organizame.modelo.TipoActividad
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class ResumenSemanalTest {

    @Test
    fun calculaContadoresSoloDentroDeLaSemana() {
        val inicio = LocalDate.of(2026, 11, 23)
        val actividades = listOf(
            tarea("Pendiente", inicio, EstadoTarea.PENDIENTE),
            tarea("Completada", inicio.plusDays(1), EstadoTarea.COMPLETADA),
            Actividad(
                tipo = TipoActividad.EVENTO,
                titulo = "Evento",
                fecha = inicio.plusDays(6)
            ),
            tarea("Fuera", inicio.plusDays(7), EstadoTarea.PENDIENTE)
        )

        val resumen = CalculadorResumenSemanal.calcular(
            actividades,
            inicio,
            LocalDateTime.of(2026, 11, 23, 12, 0)
        )

        assertEquals(3, resumen.actividades.size)
        assertEquals(1, resumen.pendientes)
        assertEquals(1, resumen.completadas)
        assertEquals(1, resumen.eventos)
    }

    @Test
    fun semanaSiempreComienzaEnLunes() {
        assertEquals(
            LocalDate.of(2026, 11, 23),
            CalculadorResumenSemanal.inicioDeSemana(LocalDate.of(2026, 11, 29))
        )
    }

    private fun tarea(
        titulo: String,
        fecha: LocalDate,
        estado: EstadoTarea
    ) = Actividad(
        tipo = TipoActividad.TAREA,
        titulo = titulo,
        fecha = fecha,
        estadoTarea = estado
    )
}
