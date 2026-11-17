package com.santi.organizame.parser

import com.santi.organizame.modelo.Prioridad
import com.santi.organizame.modelo.TipoActividad
import java.time.LocalDate
import java.time.LocalTime
import com.santi.organizame.modelo.AnticipacionRecordatorio

enum class EstadoCampoParser {
    EXTRAIDO,
    PREDETERMINADO,
    FALTANTE,
    AMBIGUO,
    INVALIDO
}

data class CampoParser<T>(
    val valor: T?,
    val estado: EstadoCampoParser
)

data class ResultadoParser(
    val textoOriginal: String,
    val titulo: CampoParser<String>,
    val tipo: CampoParser<TipoActividad>,
    val fecha: CampoParser<LocalDate>,
    val hora: CampoParser<LocalTime>,
    val prioridad: CampoParser<Prioridad>,
    val categoriaId: CampoParser<Long>,
    val advertencias: List<String>,
    val textoRestante: String,
    val descripcion: CampoParser<String> =
        CampoParser(null, EstadoCampoParser.FALTANTE),
    val recordatorio: CampoParser<AnticipacionRecordatorio> =
        CampoParser(null, EstadoCampoParser.FALTANTE)
) {
    val requiereRevision: Boolean
        get() = advertencias.isNotEmpty() || listOf(
            titulo.estado,
            tipo.estado,
            fecha.estado,
            hora.estado,
            prioridad.estado,
            categoriaId.estado,
            descripcion.estado,
            recordatorio.estado
        ).any {
            it == EstadoCampoParser.AMBIGUO ||
                    it == EstadoCampoParser.INVALIDO
        }
}