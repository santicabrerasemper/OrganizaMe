package com.santi.organizame.filtros

import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.EstadoTarea
import com.santi.organizame.modelo.Prioridad
import com.santi.organizame.modelo.TipoActividad
import java.text.Normalizer
import java.util.Locale

data class CriteriosFiltroActividad(
    val texto: String = "",
    val categoriaId: Long? = null,
    val soloSinCategoria: Boolean = false,
    val estado: EstadoTarea? = null,
    val prioridad: Prioridad? = null,
    val tipo: TipoActividad? = null
)

object FiltroActividades {

    fun aplicar(
        actividades: List<Actividad>,
        criterios: CriteriosFiltroActividad
    ): List<Actividad> {
        val textoBuscado = normalizar(criterios.texto.trim())

        return actividades.filter { actividad ->
            coincideCategoria(actividad, criterios) &&
                (criterios.estado == null || actividad.estadoTarea == criterios.estado) &&
                (criterios.prioridad == null || actividad.prioridad == criterios.prioridad) &&
                (criterios.tipo == null || actividad.tipo == criterios.tipo) &&
                coincideTexto(actividad, textoBuscado)
        }
    }

    private fun coincideCategoria(
        actividad: Actividad,
        criterios: CriteriosFiltroActividad
    ): Boolean = when {
        criterios.soloSinCategoria -> actividad.categoriaId == null
        criterios.categoriaId != null -> actividad.categoriaId == criterios.categoriaId
        else -> true
    }

    private fun coincideTexto(actividad: Actividad, textoBuscado: String): Boolean {
        if (textoBuscado.isBlank()) return true

        val contenido = normalizar(
            "${actividad.titulo} ${actividad.descripcion.orEmpty()}"
        )
        val palabras = textoBuscado
            .split(Regex("\\s+"))
            .filter(String::isNotBlank)

        return palabras.all(contenido::contains)
    }

    private fun normalizar(texto: String): String =
        Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase(Locale.ROOT)
}
