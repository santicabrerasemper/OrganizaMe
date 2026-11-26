package com.santi.organizame.ui.categorias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.santi.organizame.domain.usecase.CargarCategoriasUseCase
import com.santi.organizame.domain.usecase.EliminarCategoriaUseCase
import com.santi.organizame.domain.usecase.GuardarCategoriaUseCase
import com.santi.organizame.modelo.Categoria
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CategoriasUiState(
    val cargando: Boolean = false,
    val categorias: List<Categoria> = emptyList(),
    val errorCarga: Boolean = false
)

sealed interface CategoriasEfecto {
    data object Guardado : CategoriasEfecto
    data class Mensaje(val texto: String, val corto: Boolean = false) : CategoriasEfecto
}

class CategoriasViewModel(
    private val cargarCategorias: CargarCategoriasUseCase,
    private val guardarCategoria: GuardarCategoriaUseCase,
    private val eliminarCategoria: EliminarCategoriaUseCase
) : ViewModel() {
    private val _estado = MutableStateFlow(CategoriasUiState())
    val estado: StateFlow<CategoriasUiState> = _estado.asStateFlow()

    private val _efectos = MutableSharedFlow<CategoriasEfecto>(extraBufferCapacity = 3)
    val efectos: SharedFlow<CategoriasEfecto> = _efectos.asSharedFlow()

    fun cargar() {
        viewModelScope.launch { recargar(mostrarIndicador = true) }
    }

    fun guardar(categoria: Categoria) {
        viewModelScope.launch {
            runCatching { guardarCategoria(categoria) }
                .onSuccess {
                    _efectos.emit(CategoriasEfecto.Guardado)
                    recargar(mostrarIndicador = false)
                }
                .onFailure {
                    _efectos.emit(
                        CategoriasEfecto.Mensaje(
                            if (categoria.id == 0L) {
                                "No se pudo guardar. Revisá si el nombre ya existe."
                            } else {
                                "No se pudo actualizar la categoría."
                            }
                        )
                    )
                }
        }
    }

    fun eliminar(categoria: Categoria) {
        viewModelScope.launch {
            runCatching { eliminarCategoria(categoria) }
                .onSuccess {
                    recargar(mostrarIndicador = false)
                    _efectos.emit(
                        CategoriasEfecto.Mensaje(
                            "Categoría eliminada correctamente",
                            corto = true
                        )
                    )
                }
                .onFailure {
                    _efectos.emit(
                        CategoriasEfecto.Mensaje("No se pudo eliminar la categoría")
                    )
                }
        }
    }

    private suspend fun recargar(mostrarIndicador: Boolean) {
        if (mostrarIndicador) _estado.value = _estado.value.copy(cargando = true)
        runCatching { cargarCategorias() }
            .onSuccess {
                _estado.value = CategoriasUiState(categorias = it)
            }
            .onFailure {
                _estado.value = CategoriasUiState(errorCarga = true)
            }
    }
}
