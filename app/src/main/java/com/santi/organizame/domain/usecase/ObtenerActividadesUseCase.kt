package com.santi.organizame.domain.usecase

import com.santi.organizame.domain.repository.RepositorioActividades
import com.santi.organizame.domain.recordatorios.GestorRecordatorios
import com.santi.organizame.modelo.Actividad

class ObtenerActividadesUseCase(
    private val repositorio: RepositorioActividades
) {
    suspend operator fun invoke(): List<Actividad> = repositorio.obtenerTodas()
}

class ReprogramarRecordatoriosUseCase(
    private val repositorio: RepositorioActividades,
    private val recordatorios: GestorRecordatorios
) {
    suspend operator fun invoke() {
        repositorio.obtenerTodas().forEach { recordatorios.programar(it) }
    }
}
