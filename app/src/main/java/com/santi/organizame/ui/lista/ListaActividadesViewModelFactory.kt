package com.santi.organizame.ui.lista

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.santi.organizame.data.recordatorios.ProgramadorRecordatorios
import com.santi.organizame.data.recordatorios.GestorRecordatoriosAndroid
import com.santi.organizame.data.repository.RoomActividadRepository
import com.santi.organizame.data.repository.RoomCategoriaRepository
import com.santi.organizame.domain.usecase.CambiarEstadoActividadUseCase
import com.santi.organizame.domain.usecase.CargarDatosListadoUseCase
import com.santi.organizame.domain.usecase.EliminarActividadUseCase
import com.santi.organizame.domain.usecase.FiltrarActividadesUseCase
import com.santi.organizame.domain.usecase.ObtenerNombreCategoriaUseCase
import com.santi.organizame.modelo.OrganizaMeDatabase

class ListaActividadesViewModelFactory(
    private val contexto: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ListaActividadesViewModel::class.java))

        val baseDatos = OrganizaMeDatabase.obtener(contexto.applicationContext)
        val actividades = RoomActividadRepository(baseDatos.actividadDao())
        val categorias = RoomCategoriaRepository(baseDatos.categoriaDao())
        val recordatorios = GestorRecordatoriosAndroid(
            ProgramadorRecordatorios(contexto.applicationContext)
        )

        @Suppress("UNCHECKED_CAST")
        return ListaActividadesViewModel(
            cargarDatos = CargarDatosListadoUseCase(actividades, categorias),
            filtrar = FiltrarActividadesUseCase(),
            cambiarEstadoActividad = CambiarEstadoActividadUseCase(
                actividades,
                recordatorios
            ),
            eliminarActividad = EliminarActividadUseCase(
                actividades,
                recordatorios
            ),
            obtenerNombreCategoria = ObtenerNombreCategoriaUseCase(categorias)
        ) as T
    }
}
