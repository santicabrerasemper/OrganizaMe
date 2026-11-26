package com.santi.organizame.domain.usecase

import com.santi.organizame.domain.recordatorios.CalculadorMomentoRecordatorio
import com.santi.organizame.domain.recordatorios.GestorRecordatorios
import com.santi.organizame.domain.repository.RepositorioActividades
import com.santi.organizame.domain.repository.RepositorioCategorias
import com.santi.organizame.filtros.CriteriosFiltroActividad
import com.santi.organizame.filtros.FiltroActividades
import com.santi.organizame.filtros.OrdenActividad
import com.santi.organizame.filtros.OrdenadorActividades
import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.Categoria
import com.santi.organizame.modelo.EstadoTarea
import com.santi.organizame.modelo.GeneradorRecurrencia
import com.santi.organizame.modelo.Recurrencia
import com.santi.organizame.modelo.TipoActividad
import java.time.Instant
import java.time.LocalDateTime
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class DatosListado(
    val actividades: List<Actividad>,
    val categorias: List<Categoria>
)

data class SeccionesActividades(
    val pendientes: List<Actividad>,
    val vencidas: List<Actividad>,
    val completadas: List<Actividad>,
    val eventos: List<Actividad>
)

data class ResultadoCambioEstado(
    val actividad: Actividad,
    val siguienteActividad: Actividad? = null,
    val falloSiguienteRecurrencia: Boolean = false
)

class CargarDatosListadoUseCase(
    private val actividades: RepositorioActividades,
    private val categorias: RepositorioCategorias
) {
    suspend operator fun invoke(): DatosListado = coroutineScope {
        val actividadesPendientes = async { actividades.obtenerTodas() }
        val categoriasPendientes = async { categorias.obtenerTodas() }
        DatosListado(
            actividades = actividadesPendientes.await(),
            categorias = categoriasPendientes.await()
        )
    }
}

class FiltrarActividadesUseCase {
    operator fun invoke(
        actividades: List<Actividad>,
        criterios: CriteriosFiltroActividad,
        orden: OrdenActividad,
        ahora: LocalDateTime = LocalDateTime.now()
    ): SeccionesActividades {
        val filtradas = FiltroActividades.aplicar(actividades, criterios)
        return SeccionesActividades(
            pendientes = OrdenadorActividades.ordenar(
                filtradas.filter {
                    it.tipo == TipoActividad.TAREA &&
                        it.estadoTarea == EstadoTarea.PENDIENTE &&
                        !estaVencida(it, ahora)
                },
                orden
            ),
            vencidas = OrdenadorActividades.ordenar(
                filtradas.filter { estaVencida(it, ahora) },
                orden
            ),
            completadas = OrdenadorActividades.ordenar(
                filtradas.filter {
                    it.tipo == TipoActividad.TAREA &&
                        it.estadoTarea == EstadoTarea.COMPLETADA
                },
                orden
            ),
            eventos = OrdenadorActividades.ordenar(
                filtradas.filter { it.tipo == TipoActividad.EVENTO },
                orden
            )
        )
    }

    private fun estaVencida(actividad: Actividad, ahora: LocalDateTime): Boolean {
        if (actividad.tipo != TipoActividad.TAREA ||
            actividad.estadoTarea != EstadoTarea.PENDIENTE
        ) return false

        val fecha = actividad.fecha ?: return false
        return fecha.isBefore(ahora.toLocalDate()) ||
            (fecha == ahora.toLocalDate() &&
                actividad.hora?.isBefore(ahora.toLocalTime()) == true)
    }
}

class CambiarEstadoActividadUseCase(
    private val repositorio: RepositorioActividades,
    private val recordatorios: GestorRecordatorios,
    private val ahora: () -> Instant = Instant::now,
    private val ahoraLocal: () -> LocalDateTime = LocalDateTime::now
) {
    suspend operator fun invoke(actividad: Actividad): ResultadoCambioEstado {
        require(actividad.tipo == TipoActividad.TAREA)

        val momento = ahora()
        val completar = actividad.estadoTarea == EstadoTarea.PENDIENTE
        val crearSiguiente = completar &&
            actividad.recurrencia != Recurrencia.SIN_REPETICION &&
            !actividad.recurrenciaProcesada &&
            actividad.fecha != null

        val actualizada = if (completar) {
            actividad.copy(
                estadoTarea = EstadoTarea.COMPLETADA,
                fechaCompletada = momento,
                fechaRecordatorioProgramado = null,
                recurrenciaProcesada = actividad.recurrenciaProcesada || crearSiguiente,
                fechaModificacion = momento
            )
        } else {
            actividad.copy(
                estadoTarea = EstadoTarea.PENDIENTE,
                fechaCompletada = null,
                fechaRecordatorioProgramado =
                    CalculadorMomentoRecordatorio.calcular(
                        actividad.fecha,
                        actividad.hora,
                        actividad.recordatorio
                    )?.takeIf { it.isAfter(momento) },
                fechaModificacion = momento
            )
        }

        repositorio.actualizar(actualizada)
        if (completar) recordatorios.cancelar(actualizada.id)
        else recordatorios.programar(actualizada)

        if (!crearSiguiente) return ResultadoCambioEstado(actualizada)

        val siguienteBase = GeneradorRecurrencia.crearSiguiente(
            actividadCompletada = actualizada,
            momento = momento,
            ahoraLocal = ahoraLocal()
        ) ?: return ResultadoCambioEstado(actualizada)

        val siguiente = siguienteBase.copy(
            fechaRecordatorioProgramado = CalculadorMomentoRecordatorio.calcular(
                siguienteBase.fecha,
                siguienteBase.hora,
                siguienteBase.recordatorio
            )?.takeIf { it.isAfter(momento) }
        )

        return runCatching {
            val id = repositorio.guardar(siguiente)
            val guardada = siguiente.copy(id = id)
            recordatorios.programar(guardada)
            ResultadoCambioEstado(actualizada, guardada)
        }.getOrElse {
            ResultadoCambioEstado(
                actividad = actualizada,
                falloSiguienteRecurrencia = true
            )
        }
    }
}

class EliminarActividadUseCase(
    private val repositorio: RepositorioActividades,
    private val recordatorios: GestorRecordatorios
) {
    suspend operator fun invoke(actividad: Actividad) {
        repositorio.eliminar(actividad)
        recordatorios.cancelar(actividad.id)
    }
}

class ObtenerNombreCategoriaUseCase(
    private val categorias: RepositorioCategorias
) {
    suspend operator fun invoke(actividad: Actividad): String {
        val id = actividad.categoriaId ?: return "Sin categoría"
        return categorias.obtenerPorId(id)?.nombre ?: "Sin categoría"
    }
}
