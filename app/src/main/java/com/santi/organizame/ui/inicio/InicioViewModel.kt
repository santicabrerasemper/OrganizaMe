package com.santi.organizame.ui.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.santi.organizame.domain.usecase.DetalleActividad
import com.santi.organizame.domain.usecase.InterpretarFraseUseCase
import com.santi.organizame.domain.usecase.ObtenerDetalleActividadUseCase
import com.santi.organizame.parser.ResultadoParser
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InicioUiState(val interpretando: Boolean = false)

sealed interface InicioEfecto {
    data class AbrirFormulario(val resultado: ResultadoParser) : InicioEfecto
    data class MostrarDetalle(val detalle: DetalleActividad) : InicioEfecto
    data class Mensaje(val texto: String) : InicioEfecto
}

class InicioViewModel(
    private val interpretarFrase: InterpretarFraseUseCase,
    private val obtenerDetalle: ObtenerDetalleActividadUseCase
) : ViewModel() {
    private val _estado = MutableStateFlow(InicioUiState())
    val estado: StateFlow<InicioUiState> = _estado.asStateFlow()

    private val _efectos = MutableSharedFlow<InicioEfecto>(extraBufferCapacity = 3)
    val efectos: SharedFlow<InicioEfecto> = _efectos.asSharedFlow()

    fun interpretar(frase: String) {
        if (_estado.value.interpretando) return
        viewModelScope.launch {
            _estado.value = InicioUiState(interpretando = true)
            runCatching { interpretarFrase(frase) }
                .onSuccess { _efectos.emit(InicioEfecto.AbrirFormulario(it)) }
                .onFailure {
                    _efectos.emit(
                        InicioEfecto.Mensaje(
                            "No se pudieron cargar las categorías para interpretar la frase"
                        )
                    )
                }
            _estado.value = InicioUiState()
        }
    }

    fun abrirActividad(actividadId: Long) {
        viewModelScope.launch {
            runCatching { obtenerDetalle(actividadId) }
                .onSuccess { detalle ->
                    if (detalle == null) {
                        _efectos.emit(InicioEfecto.Mensaje("La actividad ya no existe"))
                    } else {
                        _efectos.emit(InicioEfecto.MostrarDetalle(detalle))
                    }
                }
                .onFailure {
                    _efectos.emit(InicioEfecto.Mensaje("No se pudo abrir la actividad"))
                }
        }
    }
}
