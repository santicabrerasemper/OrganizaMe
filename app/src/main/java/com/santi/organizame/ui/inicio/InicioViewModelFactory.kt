package com.santi.organizame.ui.inicio

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.santi.organizame.data.repository.RoomActividadRepository
import com.santi.organizame.data.repository.RoomCategoriaRepository
import com.santi.organizame.domain.usecase.InterpretarFraseUseCase
import com.santi.organizame.domain.usecase.ObtenerDetalleActividadUseCase
import com.santi.organizame.modelo.OrganizaMeDatabase

class InicioViewModelFactory(
    private val contexto: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(InicioViewModel::class.java))
        val baseDatos = OrganizaMeDatabase.obtener(contexto.applicationContext)
        val actividades = RoomActividadRepository(baseDatos.actividadDao())
        val categorias = RoomCategoriaRepository(baseDatos.categoriaDao())

        @Suppress("UNCHECKED_CAST")
        return InicioViewModel(
            interpretarFrase = InterpretarFraseUseCase(categorias),
            obtenerDetalle = ObtenerDetalleActividadUseCase(
                actividades,
                categorias
            )
        ) as T
    }
}
