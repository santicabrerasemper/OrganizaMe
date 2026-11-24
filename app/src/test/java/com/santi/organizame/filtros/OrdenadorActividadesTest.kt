package com.santi.organizame.filtros

import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.Prioridad
import com.santi.organizame.modelo.TipoActividad
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class OrdenadorActividadesTest {

    @Test
    fun ordenPredeterminadoUsaFechaHoraPrioridadYTitulo() {
        val actividades = listOf(
            actividad("Zeta", Prioridad.ALTA, LocalTime.of(10, 0)),
            actividad("Álgebra", Prioridad.ALTA, LocalTime.of(10, 0)),
            actividad("Temprana", Prioridad.BAJA, LocalTime.of(9, 0)),
            actividad("Media", Prioridad.MEDIA, LocalTime.of(10, 0))
        )

        val ordenadas = OrdenadorActividades.ordenar(
            actividades,
            OrdenActividad.FECHA_HORA
        )

        assertEquals(
            listOf("Temprana", "Álgebra", "Zeta", "Media"),
            ordenadas.map(Actividad::titulo)
        )
    }

    @Test
    fun puedeOrdenarPorPrioridad() {
        val actividades = listOf(
            actividad("Baja", Prioridad.BAJA),
            actividad("Alta", Prioridad.ALTA),
            actividad("Media", Prioridad.MEDIA)
        )

        assertEquals(
            listOf("Alta", "Media", "Baja"),
            OrdenadorActividades.ordenar(actividades, OrdenActividad.PRIORIDAD)
                .map(Actividad::titulo)
        )
    }

    @Test
    fun ordenPorTituloIgnoraTildesYMayusculas() {
        val actividades = listOf(
            actividad("zoológico"),
            actividad("Álgebra"),
            actividad("biblioteca")
        )

        assertEquals(
            listOf("Álgebra", "biblioteca", "zoológico"),
            OrdenadorActividades.ordenar(actividades, OrdenActividad.TITULO)
                .map(Actividad::titulo)
        )
    }

    @Test
    fun ordenRecienteUsaFechaDeCreacionDescendente() {
        val vieja = actividad("Vieja").copy(
            fechaCreacion = Instant.parse("2026-01-01T00:00:00Z")
        )
        val nueva = actividad("Nueva").copy(
            fechaCreacion = Instant.parse("2026-02-01T00:00:00Z")
        )

        assertEquals(
            listOf("Nueva", "Vieja"),
            OrdenadorActividades.ordenar(listOf(vieja, nueva), OrdenActividad.RECIENTES)
                .map(Actividad::titulo)
        )
    }

    private fun actividad(
        titulo: String,
        prioridad: Prioridad = Prioridad.MEDIA,
        hora: LocalTime = LocalTime.NOON
    ) = Actividad(
        tipo = TipoActividad.TAREA,
        titulo = titulo,
        fecha = LocalDate.of(2026, 12, 1),
        hora = hora,
        prioridad = prioridad
    )
}
