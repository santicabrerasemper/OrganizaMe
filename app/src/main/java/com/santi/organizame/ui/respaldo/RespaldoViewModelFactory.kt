package com.santi.organizame.ui.respaldo

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.santi.organizame.ProgramadorRecordatorios
import com.santi.organizame.data.recordatorios.GestorRecordatoriosAndroid
import com.santi.organizame.data.respaldo.RoomRespaldoRepository
import com.santi.organizame.domain.usecase.AnalizarRespaldoUseCase
import com.santi.organizame.domain.usecase.ImportarRespaldoUseCase
import com.santi.organizame.domain.usecase.PrepararRespaldoUseCase
import com.santi.organizame.modelo.OrganizaMeDatabase

class RespaldoViewModelFactory(
    private val contexto: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(RespaldoViewModel::class.java))
        val aplicacion = contexto.applicationContext
        val repositorio = RoomRespaldoRepository(OrganizaMeDatabase.obtener(aplicacion))
        val recordatorios = GestorRecordatoriosAndroid(ProgramadorRecordatorios(aplicacion))

        @Suppress("UNCHECKED_CAST")
        return RespaldoViewModel(
            prepararRespaldo = PrepararRespaldoUseCase(repositorio),
            analizarRespaldo = AnalizarRespaldoUseCase(),
            importarRespaldo = ImportarRespaldoUseCase(repositorio, recordatorios)
        ) as T
    }
}
