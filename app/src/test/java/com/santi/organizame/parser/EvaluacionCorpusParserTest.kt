package com.santi.organizame.parser

import com.santi.organizame.modelo.Categoria
import com.santi.organizame.modelo.Prioridad
import com.santi.organizame.modelo.TipoActividad
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class EvaluacionCorpusParserTest {

    private val parser = ParserActividad()
    private val ahora = ZonedDateTime.of(
        2026,
        11,
        16,
        15,
        0,
        0,
        0,
        ZoneId.of("America/Argentina/Buenos_Aires")
    )
    private val categorias = listOf(
        Categoria(1, "Personal", "#4CAF50", listOf("casa", "hogar")),
        Categoria(2, "Estudio", "#2196F3", listOf("facultad", "universidad")),
        Categoria(3, "Trabajo", "#FF9800", listOf("oficina", "laboral"))
    )

    @Test
    fun precisionDelCorpusCumpleElObjetivoDelMvp() {
        val casos = cargarCorpus()
        assertTrue("El corpus debe contener al menos 30 frases", casos.size >= 30)

        val aciertos = mutableMapOf(
            "titulo" to 0,
            "tipo" to 0,
            "fecha" to 0,
            "hora" to 0,
            "prioridad" to 0,
            "categoria" to 0
        )
        var coincidenciasExactas = 0

        casos.forEach { caso ->
            val resultado = parser.interpretar(caso.frase, ahora, categorias)
            val comparaciones = mapOf(
                "titulo" to (resultado.titulo.valor == caso.titulo),
                "tipo" to (resultado.tipo.valor == caso.tipo),
                "fecha" to (resultado.fecha.valor == caso.fecha),
                "hora" to (resultado.hora.valor == caso.hora),
                "prioridad" to (resultado.prioridad.valor == caso.prioridad),
                "categoria" to (resultado.categoriaId.valor == caso.categoriaId)
            )

            comparaciones.forEach { (campo, coincide) ->
                if (coincide) aciertos[campo] = aciertos.getValue(campo) + 1
            }
            if (comparaciones.values.all { it }) coincidenciasExactas++
        }

        val precisiones = aciertos.mapValues { (_, cantidad) ->
            cantidad.toDouble() / casos.size
        }
        val precisionExacta = coincidenciasExactas.toDouble() / casos.size

        println("Evaluación del parser (${casos.size} frases)")
        precisiones.forEach { (campo, precision) ->
            println("$campo: ${"%.2f".format(precision * 100)} %")
        }
        println("coincidencia exacta: ${"%.2f".format(precisionExacta * 100)} %")

        precisiones.forEach { (campo, precision) ->
            assertTrue(
                "La precisión de $campo debe ser al menos 80 %, fue $precision",
                precision >= UMBRAL_MINIMO
            )
        }
        assertTrue(
            "La coincidencia exacta debe ser al menos 80 %, fue $precisionExacta",
            precisionExacta >= UMBRAL_MINIMO
        )
    }

    private fun cargarCorpus(): List<CasoCorpus> {
        val recurso = checkNotNull(
            javaClass.classLoader?.getResourceAsStream("corpus_parser.csv")
        ) { "No se encontró corpus_parser.csv" }

        return recurso.bufferedReader(Charsets.UTF_8).useLines { lineas ->
            lineas.drop(1)
                .filter(String::isNotBlank)
                .map { linea ->
                    val campos = linea.split('|')
                    require(campos.size == 8) { "Fila inválida del corpus: $linea" }
                    CasoCorpus(
                        frase = campos[1],
                        titulo = campos[2],
                        tipo = TipoActividad.valueOf(campos[3]),
                        fecha = campos[4].valorOpcional()?.let(LocalDate::parse),
                        hora = campos[5].valorOpcional()?.let(LocalTime::parse),
                        prioridad = Prioridad.valueOf(campos[6]),
                        categoriaId = campos[7].valorOpcional()?.toLong()
                    )
                }
                .toList()
        }
    }

    private fun String.valorOpcional(): String? = takeUnless { it == "-" }

    private data class CasoCorpus(
        val frase: String,
        val titulo: String,
        val tipo: TipoActividad,
        val fecha: LocalDate?,
        val hora: LocalTime?,
        val prioridad: Prioridad,
        val categoriaId: Long?
    )

    private companion object {
        const val UMBRAL_MINIMO = 0.80
    }
}
