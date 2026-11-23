package com.santi.organizame.filtros

import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.EstadoTarea
import com.santi.organizame.modelo.Prioridad
import com.santi.organizame.modelo.TipoActividad
import org.junit.Assert.assertEquals
import org.junit.Test

class FiltroActividadesTest {

    private val actividades = listOf(
        Actividad(
            id = 1,
            tipo = TipoActividad.TAREA,
            titulo = "Estudiar álgebra",
            descripcion = "Preparar el examen final",
            prioridad = Prioridad.ALTA,
            categoriaId = 2,
            estadoTarea = EstadoTarea.PENDIENTE
        ),
        Actividad(
            id = 2,
            tipo = TipoActividad.TAREA,
            titulo = "Comprar alimentos",
            descripcion = "Leche y café",
            prioridad = Prioridad.MEDIA,
            categoriaId = null,
            estadoTarea = EstadoTarea.COMPLETADA
        ),
        Actividad(
            id = 3,
            tipo = TipoActividad.EVENTO,
            titulo = "Reunión de equipo",
            descripcion = "Presentación del proyecto",
            prioridad = Prioridad.ALTA,
            categoriaId = 3,
            estadoTarea = null
        )
    )

    @Test
    fun buscaEnTituloSinDistinguirMayusculasNiTildes() {
        val resultado = FiltroActividades.aplicar(
            actividades,
            CriteriosFiltroActividad(texto = "ALGEBRA")
        )

        assertEquals(listOf(1L), resultado.map(Actividad::id))
    }

    @Test
    fun buscaTambienEnLaDescripcion() {
        val resultado = FiltroActividades.aplicar(
            actividades,
            CriteriosFiltroActividad(texto = "cafe")
        )

        assertEquals(listOf(2L), resultado.map(Actividad::id))
    }

    @Test
    fun buscaVariasPalabrasSinTildesEntreTituloYDescripcion() {
        val resultado = FiltroActividades.aplicar(
            actividades,
            CriteriosFiltroActividad(texto = "reunion presentacion")
        )

        assertEquals(listOf(3L), resultado.map(Actividad::id))
    }

    @Test
    fun exigeQueTodasLasPalabrasBuscadasCoincidan() {
        val resultado = FiltroActividades.aplicar(
            actividades,
            CriteriosFiltroActividad(texto = "algebra inexistente")
        )

        assertEquals(emptyList<Actividad>(), resultado)
    }

    @Test
    fun filtraUnaCategoriaEspecifica() {
        val resultado = FiltroActividades.aplicar(
            actividades,
            CriteriosFiltroActividad(categoriaId = 3)
        )

        assertEquals(listOf(3L), resultado.map(Actividad::id))
    }

    @Test
    fun filtraActividadesSinCategoria() {
        val resultado = FiltroActividades.aplicar(
            actividades,
            CriteriosFiltroActividad(soloSinCategoria = true)
        )

        assertEquals(listOf(2L), resultado.map(Actividad::id))
    }

    @Test
    fun combinaLosFiltrosConCondicionAnd() {
        val resultado = FiltroActividades.aplicar(
            actividades,
            CriteriosFiltroActividad(
                texto = "proyecto",
                categoriaId = 3,
                prioridad = Prioridad.ALTA,
                tipo = TipoActividad.EVENTO
            )
        )

        assertEquals(listOf(3L), resultado.map(Actividad::id))
    }

    @Test
    fun excluyeResultadosSiUnFiltroNoCoincide() {
        val resultado = FiltroActividades.aplicar(
            actividades,
            CriteriosFiltroActividad(
                texto = "proyecto",
                estado = EstadoTarea.PENDIENTE,
                tipo = TipoActividad.EVENTO
            )
        )

        assertEquals(emptyList<Actividad>(), resultado)
    }

    @Test
    fun filtraPorEstadoYPrioridad() {
        val resultado = FiltroActividades.aplicar(
            actividades,
            CriteriosFiltroActividad(
                estado = EstadoTarea.PENDIENTE,
                prioridad = Prioridad.ALTA
            )
        )

        assertEquals(listOf(1L), resultado.map(Actividad::id))
    }

    @Test
    fun criteriosVaciosConservanElOrdenYLosDatos() {
        val resultado = FiltroActividades.aplicar(
            actividades,
            CriteriosFiltroActividad()
        )

        assertEquals(actividades, resultado)
    }
}
