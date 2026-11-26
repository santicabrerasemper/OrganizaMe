package com.santi.organizame.domain.repository

import com.santi.organizame.modelo.Categoria

interface RepositorioCategorias {
    suspend fun guardar(categoria: Categoria): Long
    suspend fun obtenerTodas(): List<Categoria>
    suspend fun actualizar(categoria: Categoria)
    suspend fun eliminar(categoria: Categoria)
    suspend fun obtenerPorId(id: Long): Categoria?
}
