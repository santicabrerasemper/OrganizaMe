package com.santi.organizame.domain.usecase

import com.santi.organizame.domain.recordatorios.GestorRecordatorios
import com.santi.organizame.domain.recordatorios.ResultadoProgramacionRecordatorio
import com.santi.organizame.domain.repository.RepositorioActividades
import com.santi.organizame.domain.repository.RepositorioCategorias
import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.Categoria
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class DatosFormularioActividad(
    val categorias: List<Categoria>,
    val actividad: Actividad?
)

data class ResultadoGuardadoActividad(
    val actividad: Actividad,
    val programacion: ResultadoProgramacionRecordatorio
)

class CargarFormularioActividadUseCase(
    private val actividades: RepositorioActividades,
    private val categorias: RepositorioCategorias
) {
    suspend operator fun invoke(actividadId: Long?): DatosFormularioActividad =
        coroutineScope {
            val categoriasPendientes = async { categorias.obtenerTodas() }
            val actividadPendiente = async {
                actividadId?.let { actividades.obtenerPorId(it) }
            }
            DatosFormularioActividad(
                categorias = categoriasPendientes.await(),
                actividad = actividadPendiente.await()
            )
        }
}

class GuardarActividadUseCase(
    private val actividades: RepositorioActividades,
    private val recordatorios: GestorRecordatorios
) {
    suspend operator fun invoke(actividad: Actividad): ResultadoGuardadoActividad {
        val guardada = if (actividad.id == 0L) {
            actividad.copy(id = actividades.guardar(actividad))
        } else {
            actividades.actualizar(actividad)
            actividad
        }

        return ResultadoGuardadoActividad(
            actividad = guardada,
            programacion = recordatorios.programar(guardada)
        )
    }
}
