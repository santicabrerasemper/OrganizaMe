package com.santi.organizame.data.repository

import com.santi.organizame.domain.repository.RepositorioCategorias
import com.santi.organizame.modelo.Categoria
import com.santi.organizame.modelo.CategoriaDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoomCategoriaRepository(
    private val dao: CategoriaDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : RepositorioCategorias {
    override suspend fun guardar(categoria: Categoria): Long =
        withContext(dispatcher) { dao.insertar(categoria) }

    override suspend fun obtenerTodas(): List<Categoria> =
        withContext(dispatcher) { dao.obtenerTodas() }

    override suspend fun actualizar(categoria: Categoria) {
        withContext(dispatcher) { dao.actualizar(categoria) }
    }

    override suspend fun eliminar(categoria: Categoria) {
        withContext(dispatcher) { dao.eliminar(categoria) }
    }

    override suspend fun obtenerPorId(id: Long): Categoria? =
        withContext(dispatcher) { dao.obtenerPorId(id) }
}
