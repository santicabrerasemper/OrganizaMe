package com.santi.organizame.domain.usecase

import com.santi.organizame.domain.repository.RepositorioCategorias
import com.santi.organizame.modelo.Categoria

class CargarCategoriasUseCase(
    private val repositorio: RepositorioCategorias
) {
    suspend operator fun invoke(): List<Categoria> = repositorio.obtenerTodas()
}

class GuardarCategoriaUseCase(
    private val repositorio: RepositorioCategorias
) {
    suspend operator fun invoke(categoria: Categoria) {
        if (categoria.id == 0L) repositorio.guardar(categoria)
        else repositorio.actualizar(categoria)
    }
}

class EliminarCategoriaUseCase(
    private val repositorio: RepositorioCategorias
) {
    suspend operator fun invoke(categoria: Categoria) = repositorio.eliminar(categoria)
}
