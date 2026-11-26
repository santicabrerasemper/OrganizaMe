package com.santi.organizame.ui.categorias

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.santi.organizame.data.repository.RoomCategoriaRepository
import com.santi.organizame.domain.usecase.CargarCategoriasUseCase
import com.santi.organizame.domain.usecase.EliminarCategoriaUseCase
import com.santi.organizame.domain.usecase.GuardarCategoriaUseCase
import com.santi.organizame.modelo.OrganizaMeDatabase

class CategoriasViewModelFactory(
    private val contexto: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(CategoriasViewModel::class.java))
        val baseDatos = OrganizaMeDatabase.obtener(contexto.applicationContext)
        val repositorio = RoomCategoriaRepository(baseDatos.categoriaDao())

        @Suppress("UNCHECKED_CAST")
        return CategoriasViewModel(
            cargarCategorias = CargarCategoriasUseCase(repositorio),
            guardarCategoria = GuardarCategoriaUseCase(repositorio),
            eliminarCategoria = EliminarCategoriaUseCase(repositorio)
        ) as T
    }
}
