package com.santi.organizame.ui.consulta

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.santi.organizame.data.repository.RoomActividadRepository
import com.santi.organizame.domain.usecase.ObtenerActividadesUseCase
import com.santi.organizame.modelo.OrganizaMeDatabase

class ConsultaActividadesViewModelFactory(
    private val contexto: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ConsultaActividadesViewModel::class.java))
        val baseDatos = OrganizaMeDatabase.obtener(contexto.applicationContext)
        val repositorio = RoomActividadRepository(baseDatos.actividadDao())

        @Suppress("UNCHECKED_CAST")
        return ConsultaActividadesViewModel(
            ObtenerActividadesUseCase(repositorio)
        ) as T
    }
}
