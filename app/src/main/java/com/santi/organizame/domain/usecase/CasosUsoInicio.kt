package com.santi.organizame.domain.usecase

import com.santi.organizame.domain.repository.RepositorioActividades
import com.santi.organizame.domain.repository.RepositorioCategorias
import com.santi.organizame.modelo.Actividad
import com.santi.organizame.parser.ParserActividad
import com.santi.organizame.parser.ResultadoParser
import java.time.ZonedDateTime

data class DetalleActividad(
    val actividad: Actividad,
    val nombreCategoria: String
)

class InterpretarFraseUseCase(
    private val categorias: RepositorioCategorias,
    private val parser: ParserActividad = ParserActividad()
) {
    suspend operator fun invoke(
        frase: String,
        ahora: ZonedDateTime = ZonedDateTime.now()
    ): ResultadoParser = parser.interpretar(
        frase = frase,
        ahora = ahora,
        categorias = categorias.obtenerTodas()
    )
}

class ObtenerDetalleActividadUseCase(
    private val actividades: RepositorioActividades,
    private val categorias: RepositorioCategorias
) {
    suspend operator fun invoke(actividadId: Long): DetalleActividad? {
        val actividad = actividades.obtenerPorId(actividadId) ?: return null
        val nombreCategoria = actividad.categoriaId
            ?.let { categorias.obtenerPorId(it)?.nombre }
            ?: "Sin categoría"
        return DetalleActividad(actividad, nombreCategoria)
    }
}
