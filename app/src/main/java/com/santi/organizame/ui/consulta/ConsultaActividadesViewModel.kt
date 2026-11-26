package com.santi.organizame.ui.consulta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.santi.organizame.domain.usecase.ObtenerActividadesUseCase
import com.santi.organizame.modelo.Actividad
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConsultaActividadesUiState(
    val cargando: Boolean = false,
    val cargaCompletada: Boolean = false,
    val actividades: List<Actividad> = emptyList()
)

class ConsultaActividadesViewModel(
    private val obtenerActividades: ObtenerActividadesUseCase
) : ViewModel() {
    private val _estado = MutableStateFlow(ConsultaActividadesUiState())
    val estado: StateFlow<ConsultaActividadesUiState> = _estado.asStateFlow()

    private val _errores = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val errores: SharedFlow<String> = _errores.asSharedFlow()
    private var carga: Job? = null

    fun cargar() {
        carga?.cancel()
        carga = viewModelScope.launch {
            _estado.value = _estado.value.copy(cargando = true)
            runCatching { obtenerActividades() }
                .onSuccess {
                    _estado.value = ConsultaActividadesUiState(
                        cargaCompletada = true,
                        actividades = it
                    )
                }
                .onFailure {
                    _estado.value = _estado.value.copy(cargando = false)
                    _errores.emit("No se pudieron cargar las actividades")
                }
        }
    }
}
