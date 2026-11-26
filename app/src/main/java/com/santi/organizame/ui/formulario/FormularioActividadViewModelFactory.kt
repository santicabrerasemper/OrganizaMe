package com.santi.organizame.ui.formulario

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.santi.organizame.data.recordatorios.ProgramadorRecordatorios
import com.santi.organizame.data.recordatorios.GestorRecordatoriosAndroid
import com.santi.organizame.data.repository.RoomActividadRepository
import com.santi.organizame.data.repository.RoomCategoriaRepository
import com.santi.organizame.domain.usecase.CargarFormularioActividadUseCase
import com.santi.organizame.domain.usecase.GuardarActividadUseCase
import com.santi.organizame.modelo.OrganizaMeDatabase

class FormularioActividadViewModelFactory(
    private val contexto: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(FormularioActividadViewModel::class.java))

        val baseDatos = OrganizaMeDatabase.obtener(contexto.applicationContext)
        val actividades = RoomActividadRepository(baseDatos.actividadDao())
        val categorias = RoomCategoriaRepository(baseDatos.categoriaDao())
        val recordatorios = GestorRecordatoriosAndroid(
            ProgramadorRecordatorios(contexto.applicationContext)
        )

        @Suppress("UNCHECKED_CAST")
        return FormularioActividadViewModel(
            cargarFormulario = CargarFormularioActividadUseCase(
                actividades,
                categorias
            ),
            guardarActividad = GuardarActividadUseCase(
                actividades,
                recordatorios
            )
        ) as T
    }
}
