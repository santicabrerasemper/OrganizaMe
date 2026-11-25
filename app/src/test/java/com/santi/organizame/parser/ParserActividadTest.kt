package com.santi.organizame.parser

import com.santi.organizame.modelo.Categoria
import com.santi.organizame.modelo.AnticipacionRecordatorio
import com.santi.organizame.modelo.Prioridad
import com.santi.organizame.modelo.TipoActividad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class ParserActividadTest {

    private val parser = ParserActividad()
    private val ahora = ZonedDateTime.of(
        2026,
        11,
        16,
        15,
        0,
        0,
        0,
        ZoneId.of("America/Argentina/Buenos_Aires")
    )

    @Test
    fun interpretaFechaRelativaHoraYValoresPredeterminados() {
        val resultado = parser.interpretar(
            "estudiar mañana a las 18",
            ahora,
            emptyList()
        )

        assertEquals("estudiar", resultado.titulo.valor)
        assertEquals(TipoActividad.TAREA, resultado.tipo.valor)
        assertEquals(EstadoCampoParser.PREDETERMINADO, resultado.tipo.estado)
        assertEquals(LocalDate.of(2026, 11, 17), resultado.fecha.valor)
        assertEquals(LocalTime.of(18, 0), resultado.hora.valor)
        assertEquals(Prioridad.MEDIA, resultado.prioridad.valor)
    }

    @Test
    fun interpretaAliasDeEventoDiaSemanalYPrioridad() {
        val resultado = parser.interpretar(
            "reunión el viernes a las 10 prioridad alta",
            ahora,
            emptyList()
        )

        assertEquals("reunión", resultado.titulo.valor)
        assertEquals(TipoActividad.EVENTO, resultado.tipo.valor)
        assertEquals(LocalDate.of(2026, 11, 20), resultado.fecha.valor)
        assertEquals(LocalTime.of(10, 0), resultado.hora.valor)
        assertEquals(Prioridad.ALTA, resultado.prioridad.valor)
    }

    @Test
    fun eliminaTipoExplicitoYAliasUrgenteDelTitulo() {
        val resultado = parser.interpretar(
            "tarea comprar remedios hoy urgente",
            ahora,
            emptyList()
        )

        assertEquals("comprar remedios", resultado.titulo.valor)
        assertEquals(LocalDate.of(2026, 11, 16), resultado.fecha.valor)
        assertEquals(Prioridad.ALTA, resultado.prioridad.valor)
    }

    @Test
    fun interpretaFechaAbsolutaYAliasTurno() {
        val resultado = parser.interpretar(
            "turno dentista 15/10/2026 a las 09:30",
            ahora,
            emptyList()
        )

        assertEquals("turno dentista", resultado.titulo.valor)
        assertEquals(TipoActividad.EVENTO, resultado.tipo.valor)
        assertEquals(LocalDate.of(2026, 10, 15), resultado.fecha.valor)
        assertEquals(LocalTime.of(9, 30), resultado.hora.valor)
    }

    @Test
    fun marcaComoAmbiguosDosHorariosDiferentes() {
        val resultado = parser.interpretar(
            "estudiar a las 18 o a las 20",
            ahora,
            emptyList()
        )

        assertEquals(EstadoCampoParser.AMBIGUO, resultado.hora.estado)
        assertNull(resultado.hora.valor)
        assertTrue(resultado.requiereRevision)
    }

    @Test
    fun marcaFechaInexistenteComoInvalida() {
        val resultado = parser.interpretar(
            "pagar factura 31/02/2027",
            ahora,
            emptyList()
        )

        assertEquals(EstadoCampoParser.INVALIDO, resultado.fecha.estado)
        assertNull(resultado.fecha.valor)
        assertTrue(resultado.advertencias.any { it.contains("fecha inválida") })
    }

    @Test
    fun resuelveElDiaActualHaciaLaSemanaSiguienteSiLaHoraPaso() {
        val resultado = parser.interpretar(
            "reunión lunes a las 10",
            ahora,
            emptyList()
        )

        assertEquals(LocalDate.of(2026, 11, 23), resultado.fecha.valor)
    }

    @Test
    fun detectaCategoriaPorPalabraClave() {
        val categorias = listOf(
            Categoria(
                id = 7,
                nombre = "Estudio",
                color = "#2196F3",
                palabrasClave = listOf("facultad")
            )
        )

        val resultado = parser.interpretar(
            "estudiar álgebra facultad mañana",
            ahora,
            categorias
        )

        assertEquals(7L, resultado.categoriaId.valor)
        assertEquals("estudiar álgebra", resultado.titulo.valor)
    }

    @Test
    fun fechaSinAnioUsaLaProximaOcurrencia() {
        val resultado = parser.interpretar(
            "renovar documento 10/01",
            ahora,
            emptyList()
        )

        assertEquals(LocalDate.of(2027, 1, 10), resultado.fecha.valor)
    }

    @Test
    fun extraeDescripcionYLaExcluyeDelTitulo() {
        val resultado = parser.interpretar(
            "tarea preparar presentación mañana descripcion incluir métricas del proyecto prioridad alta",
            ahora,
            emptyList()
        )

        assertEquals("preparar presentación", resultado.titulo.valor)
        assertEquals("incluir métricas del proyecto", resultado.descripcion.valor)
        assertEquals(EstadoCampoParser.EXTRAIDO, resultado.descripcion.estado)
        assertEquals(Prioridad.ALTA, resultado.prioridad.valor)
    }

    @Test
    fun interpretaTodosLosRecordatoriosSoportados() {
        val casos = mapOf(
            "a la hora" to AnticipacionRecordatorio.A_LA_HORA,
            "10 minutos antes" to AnticipacionRecordatorio.DIEZ_MINUTOS_ANTES,
            "30 minutos antes" to AnticipacionRecordatorio.TREINTA_MINUTOS_ANTES,
            "1 hora antes" to AnticipacionRecordatorio.UNA_HORA_ANTES,
            "1 día antes" to AnticipacionRecordatorio.UN_DIA_ANTES
        )

        casos.forEach { (expresion, recordatorioEsperado) ->
            val resultado = parser.interpretar(
                "pagar factura mañana a las 18 recordatorio $expresion",
                ahora,
                emptyList()
            )

            assertEquals(recordatorioEsperado, resultado.recordatorio.valor)
            assertEquals(EstadoCampoParser.EXTRAIDO, resultado.recordatorio.estado)
            assertEquals("pagar factura", resultado.titulo.valor)
        }
    }

    @Test
    fun marcaRecordatorioNoSoportadoComoInvalido() {
        val resultado = parser.interpretar(
            "pagar factura mañana recordatorio 2 horas antes",
            ahora,
            emptyList()
        )

        assertEquals(EstadoCampoParser.INVALIDO, resultado.recordatorio.estado)
        assertNull(resultado.recordatorio.valor)
        assertTrue(resultado.advertencias.any { it.contains("formato del recordatorio") })
    }

    @Test
    fun marcaDosRecordatoriosDiferentesComoAmbiguos() {
        val resultado = parser.interpretar(
            "pagar factura mañana recordatorio a la hora recordatorio 10 minutos antes",
            ahora,
            emptyList()
        )

        assertEquals(EstadoCampoParser.AMBIGUO, resultado.recordatorio.estado)
        assertNull(resultado.recordatorio.valor)
        assertTrue(resultado.requiereRevision)
    }
}
