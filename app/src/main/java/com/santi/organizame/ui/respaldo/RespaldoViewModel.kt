package com.santi.organizame.ui.respaldo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.santi.organizame.domain.usecase.AnalizarRespaldoUseCase
import com.santi.organizame.domain.usecase.ImportarRespaldoUseCase
import com.santi.organizame.domain.usecase.PrepararRespaldoUseCase
import com.santi.organizame.domain.usecase.RespaldoPreparado
import com.santi.organizame.respaldo.DatosRespaldo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class RespaldoUiState(val procesando: Boolean = false)

sealed interface RespaldoEfecto {
    data class Exportar(val respaldo: RespaldoPreparado) : RespaldoEfecto
    data class ConfirmarImportacion(val datos: DatosRespaldo) : RespaldoEfecto
    data class Mensaje(
        val texto: String,
        val error: Boolean = false,
        val toastCorto: Boolean = false
    ) : RespaldoEfecto
}

class RespaldoViewModel(
    private val prepararRespaldo: PrepararRespaldoUseCase,
    private val analizarRespaldo: AnalizarRespaldoUseCase,
    private val importarRespaldo: ImportarRespaldoUseCase
) : ViewModel() {
    private val _estado = MutableStateFlow(RespaldoUiState())
    val estado: StateFlow<RespaldoUiState> = _estado.asStateFlow()

    private val _efectos = MutableSharedFlow<RespaldoEfecto>(extraBufferCapacity = 2)
    val efectos: SharedFlow<RespaldoEfecto> = _efectos.asSharedFlow()

    fun prepararExportacion() {
        ejecutar(
            mensajeError = "No se pudieron leer los datos"
        ) {
            _efectos.emit(RespaldoEfecto.Exportar(prepararRespaldo()))
        }
    }

    fun analizar(texto: String) {
        ejecutar(
            mensajeError = "El archivo no es un respaldo válido"
        ) {
            val datos = withContext(Dispatchers.Default) { analizarRespaldo(texto) }
            _efectos.emit(RespaldoEfecto.ConfirmarImportacion(datos))
        }
    }

    fun importar(datos: DatosRespaldo) {
        ejecutar(
            mensajeError = "No se pudo importar el respaldo"
        ) {
            importarRespaldo(datos)
            _efectos.emit(
                RespaldoEfecto.Mensaje(
                    "Respaldo importado correctamente.",
                    toastCorto = true
                )
            )
        }
    }

    private fun ejecutar(
        mensajeError: String,
        accion: suspend () -> Unit
    ) {
        if (_estado.value.procesando) return
        viewModelScope.launch {
            _estado.value = RespaldoUiState(procesando = true)
            runCatching { accion() }
                .onFailure {
                    _efectos.emit(RespaldoEfecto.Mensaje(mensajeError, error = true))
                }
            _estado.value = RespaldoUiState()
        }
    }
}
