package com.santi.organizame.ui.formulario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.santi.organizame.domain.recordatorios.ResultadoProgramacionRecordatorio
import com.santi.organizame.domain.usecase.CargarFormularioActividadUseCase
import com.santi.organizame.domain.usecase.GuardarActividadUseCase
import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.Categoria
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FormularioActividadUiState(
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val cargaCompletada: Boolean = false,
    val categorias: List<Categoria> = emptyList(),
    val actividad: Actividad? = null
)

sealed interface FormularioActividadEfecto {
    data class Guardada(
        val programacion: ResultadoProgramacionRecordatorio
    ) : FormularioActividadEfecto

    data class Mensaje(val texto: String) : FormularioActividadEfecto
}

class FormularioActividadViewModel(
    private val cargarFormulario: CargarFormularioActividadUseCase,
    private val guardarActividad: GuardarActividadUseCase
) : ViewModel() {
    private val _estado = MutableStateFlow(FormularioActividadUiState())
    val estado: StateFlow<FormularioActividadUiState> = _estado.asStateFlow()

    private val _efectos = MutableSharedFlow<FormularioActividadEfecto>(
        extraBufferCapacity = 2
    )
    val efectos: SharedFlow<FormularioActividadEfecto> = _efectos.asSharedFlow()

    private var cargaRealizada = false

    fun cargar(actividadId: Long?) {
        if (cargaRealizada) return
        cargaRealizada = true

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true) }
            runCatching { cargarFormulario(actividadId) }
                .onSuccess { datos ->
                    _estado.update {
                        it.copy(
                            cargaCompletada = true,
                            categorias = datos.categorias,
                            actividad = datos.actividad
                        )
                    }
                }
                .onFailure {
                    cargaRealizada = false
                    _efectos.emit(
                        FormularioActividadEfecto.Mensaje(
                            "No se pudieron cargar los datos del formulario"
                        )
                    )
                }
            _estado.update { it.copy(cargando = false) }
        }
    }

    fun guardar(actividad: Actividad) {
        if (_estado.value.guardando) return

        viewModelScope.launch {
            _estado.update { it.copy(guardando = true) }
            runCatching { guardarActividad(actividad) }
                .onSuccess { resultado ->
                    _efectos.emit(
                        FormularioActividadEfecto.Guardada(resultado.programacion)
                    )
                }
                .onFailure {
                    _efectos.emit(
                        FormularioActividadEfecto.Mensaje(
                            if (actividad.id == 0L) {
                                "No se pudo guardar la actividad"
                            } else {
                                "No se pudo actualizar la actividad"
                            }
                        )
                    )
                }
            _estado.update { it.copy(guardando = false) }
        }
    }
}
