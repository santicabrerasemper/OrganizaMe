package com.santi.organizame.domain.usecase

import com.santi.organizame.domain.recordatorios.GestorRecordatorios
import com.santi.organizame.domain.recordatorios.ResultadoProgramacionRecordatorio
import com.santi.organizame.domain.repository.RepositorioActividades
import com.santi.organizame.domain.repository.RepositorioCategorias
import com.santi.organizame.domain.repository.RepositorioRespaldo
import com.santi.organizame.filtros.CriteriosFiltroActividad
import com.santi.organizame.filtros.OrdenActividad
import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.AnticipacionRecordatorio
import com.santi.organizame.modelo.Categoria
import com.santi.organizame.modelo.EstadoTarea
import com.santi.organizame.modelo.Recurrencia
import com.santi.organizame.modelo.TipoActividad
import com.santi.organizame.respaldo.DatosRespaldo
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CasosUsoArquitecturaTest {

    @Test
    fun guardarActividadNueva_asignaIdYProgramaRecordatorio() = runTest {
        val repositorio = RepositorioActividadesFalso()
        val recordatorios = GestorRecordatoriosFalso()
        val actividad = tarea(
            titulo = "Entrega",
            fecha = LocalDate.of(2099, 1, 10),
            hora = LocalTime.of(10, 0),
            recordatorio = AnticipacionRecordatorio.A_LA_HORA
        )

        val resultado = GuardarActividadUseCase(repositorio, recordatorios)(actividad)

        assertEquals(1L, resultado.actividad.id)
        assertEquals(ResultadoProgramacionRecordatorio.EXACTO, resultado.programacion)
        assertEquals(listOf(1L), recordatorios.programados.map { it.id })
        assertEquals(1, repositorio.actividades.size)
    }

    @Test
    fun guardarActividadExistente_actualizaSinInsertarOtra() = runTest {
        val original = tarea(id = 7, titulo = "Original")
        val repositorio = RepositorioActividadesFalso(mutableListOf(original))
        val recordatorios = GestorRecordatoriosFalso()
        val editada = original.copy(titulo = "Editada")

        GuardarActividadUseCase(repositorio, recordatorios)(editada)

        assertEquals(1, repositorio.actividades.size)
        assertEquals("Editada", repositorio.actividades.single().titulo)
        assertEquals(listOf(7L), recordatorios.programados.map { it.id })
    }

    @Test
    fun completarTareaRecurrente_cancelaActualYCreaSiguiente() = runTest {
        val actual = tarea(
            id = 4,
            titulo = "Rutina",
            fecha = LocalDate.of(2026, 11, 20),
            hora = LocalTime.of(9, 0),
            recurrencia = Recurrencia.DIARIA
        )
        val repositorio = RepositorioActividadesFalso(mutableListOf(actual))
        val recordatorios = GestorRecordatoriosFalso()
        val momento = Instant.parse("2026-11-20T15:00:00Z")

        val resultado = CambiarEstadoActividadUseCase(
            repositorio = repositorio,
            recordatorios = recordatorios,
            ahora = { momento },
            ahoraLocal = { LocalDateTime.of(2026, 11, 20, 12, 0) }
        )(actual)

        assertEquals(EstadoTarea.COMPLETADA, resultado.actividad.estadoTarea)
        assertEquals(listOf(4L), recordatorios.cancelados)
        assertNotNull(resultado.siguienteActividad)
        assertEquals(LocalDate.of(2026, 11, 21), resultado.siguienteActividad?.fecha)
        assertEquals(EstadoTarea.PENDIENTE, resultado.siguienteActividad?.estadoTarea)
        assertFalse(resultado.falloSiguienteRecurrencia)
    }

    @Test
    fun filtrarActividades_separaPendientesVencidasCompletadasYEventos() {
        val ahora = LocalDateTime.of(2026, 11, 20, 12, 0)
        val actividades = listOf(
            tarea(id = 1, titulo = "Pendiente", fecha = LocalDate.of(2026, 11, 21)),
            tarea(id = 2, titulo = "Vencida", fecha = LocalDate.of(2026, 11, 19)),
            tarea(
                id = 3,
                titulo = "Completa",
                fecha = LocalDate.of(2026, 11, 20),
                estado = EstadoTarea.COMPLETADA
            ),
            Actividad(
                id = 4,
                tipo = TipoActividad.EVENTO,
                titulo = "Evento",
                fecha = LocalDate.of(2026, 11, 20)
            )
        )

        val resultado = FiltrarActividadesUseCase()(
            actividades,
            CriteriosFiltroActividad(),
            OrdenActividad.FECHA_HORA,
            ahora
        )

        assertEquals(listOf("Pendiente"), resultado.pendientes.map { it.titulo })
        assertEquals(listOf("Vencida"), resultado.vencidas.map { it.titulo })
        assertEquals(listOf("Completa"), resultado.completadas.map { it.titulo })
        assertEquals(listOf("Evento"), resultado.eventos.map { it.titulo })
    }

    @Test
    fun interpretarFrase_usaCategoriasDelRepositorio() = runTest {
        val categorias = RepositorioCategoriasFalso(
            mutableListOf(
                Categoria(
                    id = 3,
                    nombre = "Estudio",
                    color = "#2563EB",
                    palabrasClave = listOf("facultad")
                )
            )
        )
        val ahora = ZonedDateTime.of(
            2026,
            11,
            20,
            10,
            0,
            0,
            0,
            ZoneId.of("America/Argentina/Buenos_Aires")
        )

        val resultado = InterpretarFraseUseCase(categorias)(
            "entregar TP de facultad mañana a las 18",
            ahora
        )

        assertEquals(3L, resultado.categoriaId.valor)
        assertEquals(LocalDate.of(2026, 11, 21), resultado.fecha.valor)
        assertEquals(LocalTime.of(18, 0), resultado.hora.valor)
    }

    @Test
    fun importarRespaldo_reemplazaDatosYReprogramaAlarmas() = runTest {
        val anterior = tarea(id = 9, titulo = "Anterior")
        val nueva = tarea(
            id = 12,
            titulo = "Nueva",
            fecha = LocalDate.of(2099, 2, 1),
            hora = LocalTime.of(8, 30),
            recordatorio = AnticipacionRecordatorio.DIEZ_MINUTOS_ANTES
        )
        val repositorio = RepositorioRespaldoFalso(
            DatosRespaldo(emptyList(), listOf(anterior))
        )
        val recordatorios = GestorRecordatoriosFalso()

        ImportarRespaldoUseCase(repositorio, recordatorios)(
            DatosRespaldo(emptyList(), listOf(nueva))
        )

        assertEquals(listOf(12L), repositorio.datos.actividades.map { it.id })
        assertNotNull(repositorio.datos.actividades.single().fechaRecordatorioProgramado)
        assertEquals(listOf(9L), recordatorios.cancelados)
        assertEquals(listOf(12L), recordatorios.programados.map { it.id })
    }

    @Test
    fun obtenerDetalle_sinCategoriaDevuelveNombrePredeterminado() = runTest {
        val actividad = tarea(id = 2, titulo = "Sin categoría")
        val detalle = ObtenerDetalleActividadUseCase(
            RepositorioActividadesFalso(mutableListOf(actividad)),
            RepositorioCategoriasFalso()
        )(2)

        assertEquals("Sin categoría", detalle?.nombreCategoria)
        assertNull(
            ObtenerDetalleActividadUseCase(
                RepositorioActividadesFalso(),
                RepositorioCategoriasFalso()
            )(99)
        )
    }

    private fun tarea(
        id: Long = 0,
        titulo: String,
        fecha: LocalDate? = null,
        hora: LocalTime? = null,
        estado: EstadoTarea = EstadoTarea.PENDIENTE,
        recordatorio: AnticipacionRecordatorio? = null,
        recurrencia: Recurrencia = Recurrencia.SIN_REPETICION
    ) = Actividad(
        id = id,
        tipo = TipoActividad.TAREA,
        titulo = titulo,
        fecha = fecha,
        hora = hora,
        estadoTarea = estado,
        recordatorio = recordatorio,
        recurrencia = recurrencia
    )
}

private class RepositorioActividadesFalso(
    val actividades: MutableList<Actividad> = mutableListOf()
) : RepositorioActividades {
    private var siguienteId = (actividades.maxOfOrNull { it.id } ?: 0L) + 1L

    override suspend fun guardar(actividad: Actividad): Long {
        val id = if (actividad.id == 0L) siguienteId++ else actividad.id
        actividades += actividad.copy(id = id)
        return id
    }

    override suspend fun obtenerTodas(): List<Actividad> = actividades.toList()

    override suspend fun actualizar(actividad: Actividad) {
        val indice = actividades.indexOfFirst { it.id == actividad.id }
        if (indice >= 0) actividades[indice] = actividad else actividades += actividad
    }

    override suspend fun eliminar(actividad: Actividad) {
        actividades.removeAll { it.id == actividad.id }
    }

    override suspend fun obtenerPorId(id: Long): Actividad? =
        actividades.firstOrNull { it.id == id }
}

private class RepositorioCategoriasFalso(
    val categorias: MutableList<Categoria> = mutableListOf()
) : RepositorioCategorias {
    override suspend fun guardar(categoria: Categoria): Long {
        val id = (categorias.maxOfOrNull { it.id } ?: 0L) + 1L
        categorias += categoria.copy(id = id)
        return id
    }

    override suspend fun obtenerTodas(): List<Categoria> = categorias.toList()

    override suspend fun actualizar(categoria: Categoria) {
        val indice = categorias.indexOfFirst { it.id == categoria.id }
        if (indice >= 0) categorias[indice] = categoria
    }

    override suspend fun eliminar(categoria: Categoria) {
        categorias.removeAll { it.id == categoria.id }
    }

    override suspend fun obtenerPorId(id: Long): Categoria? =
        categorias.firstOrNull { it.id == id }
}

private class GestorRecordatoriosFalso : GestorRecordatorios {
    val programados = mutableListOf<Actividad>()
    val cancelados = mutableListOf<Long>()

    override fun programar(actividad: Actividad): ResultadoProgramacionRecordatorio {
        programados += actividad
        return ResultadoProgramacionRecordatorio.EXACTO
    }

    override fun cancelar(actividadId: Long) {
        cancelados += actividadId
    }
}

private class RepositorioRespaldoFalso(
    var datos: DatosRespaldo
) : RepositorioRespaldo {
    override suspend fun obtener(): DatosRespaldo = datos

    override suspend fun reemplazar(datos: DatosRespaldo) {
        this.datos = datos
    }
}
