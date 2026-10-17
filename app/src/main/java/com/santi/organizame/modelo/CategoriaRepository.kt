package com.santi.organizame.modelo

import java.util.concurrent.Executors

class CategoriaRepository(
    private val categoriaDao: CategoriaDao
) {

    fun guardar(
        categoria: Categoria,
        alCompletar: (Result<Long>) -> Unit
    ) {
        ejecutor.execute {
            alCompletar(
                runCatching { categoriaDao.insertar(categoria) }
            )
        }
    }

    fun obtenerTodas(
        alCompletar: (Result<List<Categoria>>) -> Unit
    ) {
        ejecutor.execute {
            alCompletar(
                runCatching { categoriaDao.obtenerTodas() }
            )
        }
    }

    fun actualizar(
        categoria: Categoria,
        alCompletar: (Result<Unit>) -> Unit
    ) {
        ejecutor.execute {
            alCompletar(
                runCatching { categoriaDao.actualizar(categoria) }
            )
        }
    }

    fun eliminar(
        categoria: Categoria,
        alCompletar: (Result<Unit>) -> Unit
    ) {
        ejecutor.execute {
            alCompletar(
                runCatching { categoriaDao.eliminar(categoria) }
            )
        }
    }

    fun obtenerPorId(
        id: Long,
        alCompletar: (Result<Categoria?>) -> Unit
    ) {
        ejecutor.execute {
            alCompletar(
                runCatching { categoriaDao.obtenerPorId(id) }
            )
        }
    }

    private companion object {
        val ejecutor = Executors.newSingleThreadExecutor()
    }
}


