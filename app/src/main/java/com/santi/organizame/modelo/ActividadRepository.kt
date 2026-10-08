package com.santi.organizame.modelo

import java.util.concurrent.Executors

class ActividadRepository(
    private val actividadDao: ActividadDao
) {
    fun guardar(
        actividad: Actividad,
        alCompletar: (Result<Long>) -> Unit
    ) {
        ejecutor.execute {
            alCompletar(
                runCatching { actividadDao.insertar(actividad) }
            )
        }
    }

    fun obtenerTodas(
        alCompletar: (Result<List<Actividad>>) -> Unit
    ) {
        ejecutor.execute {
            alCompletar(
                runCatching { actividadDao.obtenerTodas() }
            )
        }
    }

    private companion object {
        val ejecutor = Executors.newSingleThreadExecutor()
    }
}