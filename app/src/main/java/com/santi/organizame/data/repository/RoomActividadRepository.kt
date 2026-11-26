package com.santi.organizame.data.repository

import com.santi.organizame.domain.repository.RepositorioActividades
import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.ActividadDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoomActividadRepository(
    private val dao: ActividadDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : RepositorioActividades {
    override suspend fun guardar(actividad: Actividad): Long =
        withContext(dispatcher) { dao.insertar(actividad) }

    override suspend fun obtenerTodas(): List<Actividad> =
        withContext(dispatcher) { dao.obtenerTodas() }

    override suspend fun actualizar(actividad: Actividad) {
        withContext(dispatcher) { dao.actualizar(actividad) }
    }

    override suspend fun eliminar(actividad: Actividad) {
        withContext(dispatcher) { dao.eliminar(actividad) }
    }

    override suspend fun obtenerPorId(id: Long): Actividad? =
        withContext(dispatcher) { dao.obtenerPorId(id) }
}
