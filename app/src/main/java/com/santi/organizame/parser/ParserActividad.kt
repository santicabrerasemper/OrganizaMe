package com.santi.organizame.parser

import com.santi.organizame.modelo.Categoria
import com.santi.organizame.modelo.Prioridad
import com.santi.organizame.modelo.TipoActividad
import java.text.Normalizer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.Locale

class ParserActividad {

    fun interpretar(
        frase: String,
        ahora: ZonedDateTime,
        categorias: List<Categoria>
    ): ResultadoParser {
        val original = frase.trim()
        val normalizado = normalizar(original)
        val consumido = BooleanArray(original.length)
        val advertencias = mutableListOf<String>()

        if (original.isBlank()) {
            return ResultadoParser(
                textoOriginal = frase,
                titulo = CampoParser(null, EstadoCampoParser.FALTANTE),
                tipo = CampoParser(TipoActividad.TAREA, EstadoCampoParser.PREDETERMINADO),
                fecha = CampoParser(null, EstadoCampoParser.FALTANTE),
                hora = CampoParser(null, EstadoCampoParser.FALTANTE),
                prioridad = CampoParser(Prioridad.MEDIA, EstadoCampoParser.PREDETERMINADO),
                categoriaId = CampoParser(null, EstadoCampoParser.FALTANTE),
                advertencias = listOf("Ingresá una frase para interpretar."),
                textoRestante = ""
            )
        }

        if (EXPRESION_NO_SOPORTADA.containsMatchIn(normalizado)) {
            advertencias += "La expresión «la semana que viene» todavía no está soportada."
        }

        val hora = extraerHora(normalizado, consumido, advertencias)
        val fecha = extraerFecha(
            normalizado,
            ahora,
            hora.valor,
            consumido,
            advertencias
        )
        val tipo = extraerTipo(normalizado, consumido, advertencias)
        val prioridad = extraerPrioridad(normalizado, consumido, advertencias)
        val categoria = extraerCategoria(
            normalizado,
            categorias,
            consumido,
            advertencias
        )

        if (hora.valor != null && fecha.valor == null &&
            fecha.estado == EstadoCampoParser.FALTANTE
        ) {
            advertencias += "Se detectó una hora, pero falta indicar la fecha."
        }
        if (tipo.valor == TipoActividad.EVENTO && fecha.valor == null &&
            fecha.estado == EstadoCampoParser.FALTANTE
        ) {
            advertencias += "Los eventos necesitan una fecha."
        }

        val textoRestante = construirTextoRestante(original, consumido)
        val titulo: CampoParser<String> = if (textoRestante.isBlank()) {
            advertencias += "No se pudo construir un título."
            CampoParser<String>(null, EstadoCampoParser.FALTANTE)
        } else {
            CampoParser(textoRestante, EstadoCampoParser.EXTRAIDO)
        }

        return ResultadoParser(
            textoOriginal = frase,
            titulo = titulo,
            tipo = tipo,
            fecha = fecha,
            hora = hora,
            prioridad = prioridad,
            categoriaId = categoria,
            advertencias = advertencias.distinct(),
            textoRestante = textoRestante
        )
    }

    private fun extraerHora(
        texto: String,
        consumido: BooleanArray,
        advertencias: MutableList<String>
    ): CampoParser<LocalTime> {
        val horas = linkedSetOf<LocalTime>()
        var encontroInvalida = false

        PATRON_HORA.findAll(texto).forEach { coincidencia ->
            consumir(coincidencia.range, consumido)
            val hora = (coincidencia.groups[1]?.value ?: coincidencia.groups[3]?.value)
                ?.toIntOrNull()
            val minuto = (coincidencia.groups[2]?.value ?: coincidencia.groups[4]?.value)
                ?.toIntOrNull() ?: 0

            if (hora == null || hora !in 0..23 || minuto !in 0..59) {
                encontroInvalida = true
            } else {
                horas += LocalTime.of(hora, minuto)
            }
        }

        return when {
            encontroInvalida -> {
                advertencias += "Se encontró un horario inválido."
                CampoParser(null, EstadoCampoParser.INVALIDO)
            }
            horas.size > 1 -> {
                advertencias += "Se encontraron varios horarios diferentes."
                CampoParser(null, EstadoCampoParser.AMBIGUO)
            }
            horas.size == 1 -> CampoParser(horas.first(), EstadoCampoParser.EXTRAIDO)
            else -> CampoParser(null, EstadoCampoParser.FALTANTE)
        }
    }

    private fun extraerFecha(
        texto: String,
        ahora: ZonedDateTime,
        horaExtraida: LocalTime?,
        consumido: BooleanArray,
        advertencias: MutableList<String>
    ): CampoParser<LocalDate> {
        val fechas = linkedSetOf<LocalDate>()
        var encontroInvalida = false

        PATRON_FECHA_ABSOLUTA.findAll(texto).forEach { coincidencia ->
            consumir(coincidencia.range, consumido)
            val dia = coincidencia.groupValues[1].toInt()
            val mes = coincidencia.groupValues[2].toInt()
            val anioEscrito = coincidencia.groupValues[3].takeIf { it.isNotBlank() }
                ?.toInt()

            val fecha = runCatching {
                if (anioEscrito != null) {
                    LocalDate.of(anioEscrito, mes, dia)
                } else {
                    var candidata = LocalDate.of(ahora.year, mes, dia)
                    if (candidata.isBefore(ahora.toLocalDate())) {
                        candidata = candidata.plusYears(1)
                    }
                    candidata
                }
            }.getOrNull()

            if (fecha == null) encontroInvalida = true else fechas += fecha
        }

        PATRON_FECHA_RELATIVA.findAll(texto).forEach { coincidencia ->
            consumir(coincidencia.range, consumido)
            fechas += when (coincidencia.value.trim()) {
                "hoy" -> ahora.toLocalDate()
                "manana" -> ahora.toLocalDate().plusDays(1)
                else -> ahora.toLocalDate().plusDays(2)
            }
        }

        PATRON_DIA_SEMANA.findAll(texto).forEach { coincidencia ->
            if (coincidencia.range.any { consumido.getOrElse(it) { false } }) {
                return@forEach
            }
            consumir(coincidencia.range, consumido)
            val diaObjetivo = DIAS[coincidencia.groupValues[1]] ?: return@forEach
            val diaActual = ahora.dayOfWeek
            var diferencia = (diaObjetivo.value - diaActual.value + 7) % 7

            if (diferencia == 0 && horaExtraida != null &&
                !horaExtraida.isAfter(ahora.toLocalTime())
            ) {
                diferencia = 7
            } else if (diferencia == 0 && horaExtraida == null) {
                advertencias +=
                    "El día indicado es hoy y no tiene hora; revisá la fecha antes de guardar."
            }

            fechas += ahora.toLocalDate().plusDays(diferencia.toLong())
        }

        return when {
            encontroInvalida -> {
                advertencias += "Se encontró una fecha inválida."
                CampoParser(null, EstadoCampoParser.INVALIDO)
            }
            fechas.size > 1 -> {
                advertencias += "Se encontraron varias fechas diferentes."
                CampoParser(null, EstadoCampoParser.AMBIGUO)
            }
            fechas.size == 1 -> CampoParser(fechas.first(), EstadoCampoParser.EXTRAIDO)
            else -> CampoParser(null, EstadoCampoParser.FALTANTE)
        }
    }

    private fun extraerTipo(
        texto: String,
        consumido: BooleanArray,
        advertencias: MutableList<String>
    ): CampoParser<TipoActividad> {
        val tipos = linkedSetOf<TipoActividad>()

        PATRON_TIPO_EXPLICITO.findAll(texto).forEach { coincidencia ->
            consumir(coincidencia.range, consumido)
            tipos += if (coincidencia.value == "evento") {
                TipoActividad.EVENTO
            } else {
                TipoActividad.TAREA
            }
        }

        PATRON_ALIAS_EVENTO.findAll(texto).forEach { coincidencia ->
            tipos += TipoActividad.EVENTO
            if (coincidencia.value != "reunion") {
                consumir(coincidencia.range, consumido)
            }
        }

        return when {
            tipos.size > 1 -> {
                advertencias += "La frase contiene tipos de actividad incompatibles."
                CampoParser(TipoActividad.TAREA, EstadoCampoParser.AMBIGUO)
            }
            tipos.size == 1 -> CampoParser(tipos.first(), EstadoCampoParser.EXTRAIDO)
            else -> CampoParser(TipoActividad.TAREA, EstadoCampoParser.PREDETERMINADO)
        }
    }

    private fun extraerPrioridad(
        texto: String,
        consumido: BooleanArray,
        advertencias: MutableList<String>
    ): CampoParser<Prioridad> {
        val prioridades = linkedSetOf<Prioridad>()

        PATRON_PRIORIDAD.findAll(texto).forEach { coincidencia ->
            consumir(coincidencia.range, consumido)
            prioridades += when {
                coincidencia.value == "urgente" -> Prioridad.ALTA
                coincidencia.groupValues[1] == "alta" -> Prioridad.ALTA
                coincidencia.groupValues[1] == "baja" -> Prioridad.BAJA
                else -> Prioridad.MEDIA
            }
        }

        return when {
            prioridades.size > 1 -> {
                advertencias += "Se encontraron prioridades diferentes."
                CampoParser(Prioridad.MEDIA, EstadoCampoParser.AMBIGUO)
            }
            prioridades.size == 1 ->
                CampoParser(prioridades.first(), EstadoCampoParser.EXTRAIDO)
            else -> CampoParser(Prioridad.MEDIA, EstadoCampoParser.PREDETERMINADO)
        }
    }

    private fun extraerCategoria(
        texto: String,
        categorias: List<Categoria>,
        consumido: BooleanArray,
        advertencias: MutableList<String>
    ): CampoParser<Long> {
        val encontradas = linkedSetOf<Long>()
        val rangos = mutableListOf<IntRange>()

        categorias.forEach { categoria ->
            val expresiones = (listOf(categoria.nombre) + categoria.palabrasClave)
                .map(::normalizar)
                .filter(String::isNotBlank)
                .distinct()

            expresiones.forEach { expresion ->
                val patron = Regex(
                    "(?<![\\p{L}\\p{N}])${Regex.escape(expresion)}(?![\\p{L}\\p{N}])"
                )
                patron.findAll(texto).forEach { coincidencia ->
                    if (coincidencia.range.none { consumido.getOrElse(it) { false } }) {
                        encontradas += categoria.id
                        rangos += coincidencia.range
                    }
                }
            }
        }

        rangos.forEach { consumir(it, consumido) }

        return when {
            encontradas.size > 1 -> {
                advertencias += "La frase coincide con más de una categoría."
                CampoParser(null, EstadoCampoParser.AMBIGUO)
            }
            encontradas.size == 1 ->
                CampoParser(encontradas.first(), EstadoCampoParser.EXTRAIDO)
            else -> CampoParser(null, EstadoCampoParser.FALTANTE)
        }
    }

    private fun construirTextoRestante(original: String, consumido: BooleanArray): String {
        val texto = buildString {
            original.forEachIndexed { indice, caracter ->
                append(if (consumido.getOrElse(indice) { false }) ' ' else caracter)
            }
        }

        return texto
            .replace(Regex("""\s+"""), " ")
            .trim(' ', ',', '.', ';', ':', '-')
            .replace(Regex("""\s+(?:para|el|a)$""", RegexOption.IGNORE_CASE), "")
            .trim()
    }

    private fun consumir(rango: IntRange, consumido: BooleanArray) {
        rango.forEach { indice ->
            if (indice in consumido.indices) consumido[indice] = true
        }
    }

    private fun normalizar(texto: String): String =
        Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase(Locale.ROOT)

    private companion object {
        val PATRON_HORA = Regex(
            """\b(?:a\s+las?\s+(\d{1,2})(?::(\d{2}))?|(\d{1,2}):(\d{2}))\b"""
        )
        val PATRON_FECHA_ABSOLUTA = Regex(
            """\b(\d{1,2})[/-](\d{1,2})(?:[/-](\d{4}))?\b"""
        )
        val PATRON_FECHA_RELATIVA = Regex("""\b(pasado\s+manana|manana|hoy)\b""")
        val PATRON_DIA_SEMANA = Regex(
            """\b(?:el\s+)?(lunes|martes|miercoles|jueves|viernes|sabado|domingo)\b"""
        )
        val PATRON_TIPO_EXPLICITO = Regex("""\b(tarea|evento)\b""")
        val PATRON_ALIAS_EVENTO = Regex("""\b(reunion|cita|turno)\b""")
        val PATRON_PRIORIDAD = Regex("""\b(?:prioridad\s+(alta|media|baja)|urgente)\b""")
        val EXPRESION_NO_SOPORTADA = Regex("""\bla\s+semana\s+que\s+viene\b""")

        val DIAS = mapOf(
            "lunes" to DayOfWeek.MONDAY,
            "martes" to DayOfWeek.TUESDAY,
            "miercoles" to DayOfWeek.WEDNESDAY,
            "jueves" to DayOfWeek.THURSDAY,
            "viernes" to DayOfWeek.FRIDAY,
            "sabado" to DayOfWeek.SATURDAY,
            "domingo" to DayOfWeek.SUNDAY
        )
    }
}
