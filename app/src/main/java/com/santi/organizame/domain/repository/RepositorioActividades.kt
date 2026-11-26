package com.santi.organizame.domain.repository

import com.santi.organizame.modelo.Actividad

interface RepositorioActividades {
    suspend fun guardar(actividad: Actividad): Long
    suspend fun obtenerTodas(): List<Actividad>
    suspend fun actualizar(actividad: Actividad)
    suspend fun eliminar(actividad: Actividad)
    suspend fun obtenerPorId(id: Long): Actividad?
}
