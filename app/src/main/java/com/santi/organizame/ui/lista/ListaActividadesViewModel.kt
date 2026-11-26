package com.santi.organizame.ui.lista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.santi.organizame.domain.usecase.CambiarEstadoActividadUseCase
import com.santi.organizame.domain.usecase.CargarDatosListadoUseCase
import com.santi.organizame.domain.usecase.EliminarActividadUseCase
import com.santi.organizame.domain.usecase.FiltrarActividadesUseCase
import com.santi.organizame.domain.usecase.ObtenerNombreCategoriaUseCase
import com.santi.organizame.domain.usecase.SeccionesActividades
import com.santi.organizame.filtros.CriteriosFiltroActividad
import com.santi.organizame.filtros.OrdenActividad
import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.Categoria
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ListaActividadesUiState(
    val cargando: Boolean = false,
    val categorias: List<Categoria> = emptyList(),
    val secciones: SeccionesActividades = SeccionesActividades(
        pendientes = emptyList(),
        vencidas = emptyList(),
        completadas = emptyList(),
        eventos = emptyList()
    ),
    val hayFiltrosActivos: Boolean = false
)

sealed interface ListaActividadesEfecto {
    data class Mensaje(val texto: String, val largo: Boolean = false) :
        ListaActividadesEfecto

    data class MostrarDetalle(
        val actividad: Actividad,
        val nombreCategoria: String
    ) : ListaActividadesEfecto
}

class ListaActividadesViewModel(
    private val cargarDatos: CargarDatosListadoUseCase,
    private val filtrar: FiltrarActividadesUseCase,
    private val cambiarEstadoActividad: CambiarEstadoActividadUseCase,
    private val eliminarActividad: EliminarActividadUseCase,
    private val obtenerNombreCategoria: ObtenerNombreCategoriaUseCase
) : ViewModel() {
    private val _estado = MutableStateFlow(ListaActividadesUiState())
    val estado: StateFlow<ListaActividadesUiState> = _estado.asStateFlow()

    private val _efectos = MutableSharedFlow<ListaActividadesEfecto>(
        extraBufferCapacity = 4
    )
    val efectos: SharedFlow<ListaActividadesEfecto> = _efectos.asSharedFlow()

    private var actividades: List<Actividad> = emptyList()
    private var criterios = CriteriosFiltroActividad()
    private var orden = OrdenActividad.FECHA_HORA
    private var cargaActual: Job? = null

    fun cargar() {
        cargaActual?.cancel()
        cargaActual = viewModelScope.launch {
            _estado.update { it.copy(cargando = true) }
            runCatching { cargarDatos() }
                .onSuccess { datos ->
                    actividades = datos.actividades
                    _estado.update { it.copy(categorias = datos.categorias) }
                    publicarSecciones()
                }
                .onFailure {
                    _efectos.emit(
                        ListaActividadesEfecto.Mensaje(
                            "No se pudieron cargar las actividades",
                            largo = true
                        )
                    )
                }
            _estado.update { it.copy(cargando = false) }
        }
    }

    fun actualizarFiltros(
        nuevosCriterios: CriteriosFiltroActividad,
        nuevoOrden: OrdenActividad
    ) {
        criterios = nuevosCriterios
        orden = nuevoOrden
        publicarSecciones()
    }

    fun cambiarEstado(actividad: Actividad) {
        viewModelScope.launch {
            runCatching { cambiarEstadoActividad(actividad) }
                .onSuccess { resultado ->
                    if (resultado.siguienteActividad != null) {
                        _efectos.emit(
                            ListaActividadesEfecto.Mensaje(
                                "Se creó la próxima tarea repetitiva"
                            )
                        )
                    } else if (resultado.falloSiguienteRecurrencia) {
                        _efectos.emit(
                            ListaActividadesEfecto.Mensaje(
                                "La tarea se completó, pero no se pudo crear la siguiente",
                                largo = true
                            )
                        )
                    }
                    recargarSinIndicador()
                }
                .onFailure {
                    _efectos.emit(
                        ListaActividadesEfecto.Mensaje(
                            "No se pudo cambiar el estado de la tarea",
                            largo = true
                        )
                    )
                }
        }
    }

    fun eliminar(actividad: Actividad) {
        viewModelScope.launch {
            runCatching { eliminarActividad(actividad) }
                .onSuccess { recargarSinIndicador() }
                .onFailure {
                    _efectos.emit(
                        ListaActividadesEfecto.Mensaje(
                            "No se pudo eliminar la actividad",
                            largo = true
                        )
                    )
                }
        }
    }

    fun solicitarDetalle(actividad: Actividad) {
        viewModelScope.launch {
            runCatching { obtenerNombreCategoria(actividad) }
                .onSuccess { nombre ->
                    _efectos.emit(
                        ListaActividadesEfecto.MostrarDetalle(actividad, nombre)
                    )
                }
                .onFailure {
                    _efectos.emit(
                        ListaActividadesEfecto.Mensaje(
                            "No se pudo consultar la categoría"
                        )
                    )
                }
        }
    }

    private suspend fun recargarSinIndicador() {
        val datos = cargarDatos()
        actividades = datos.actividades
        _estado.update { it.copy(categorias = datos.categorias) }
        publicarSecciones()
    }

    private fun publicarSecciones() {
        val filtrosActivos = criterios.texto.isNotBlank() ||
            criterios.categoriaId != null ||
            criterios.soloSinCategoria ||
            criterios.estado != null ||
            criterios.prioridad != null ||
            criterios.tipo != null

        _estado.update {
            it.copy(
                secciones = filtrar(actividades, criterios, orden),
                hayFiltrosActivos = filtrosActivos
            )
        }
    }
}
