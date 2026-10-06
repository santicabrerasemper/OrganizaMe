package com.santi.organizame.modelo

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class ConversoresRoom {

    @TypeConverter
    fun localDateAValor(fecha: LocalDate?): Long? = fecha?.toEpochDay()

    @TypeConverter
    fun valorALocalDate(valor: Long?): LocalDate? = valor?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun localTimeAValor(hora: LocalTime?): Long? = hora?.toSecondOfDay()?.toLong()

    @TypeConverter
    fun valorALocalTime(valor: Long?): LocalTime? = valor?.let(LocalTime::ofSecondOfDay)

    @TypeConverter
    fun instantAValor(instante: Instant?): Long? = instante?.toEpochMilli()

    @TypeConverter
    fun valorAInstant(valor: Long?): Instant? = valor?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun tipoActividadAValor(tipo: TipoActividad?): String? = tipo?.name

    @TypeConverter
    fun valorATipoActividad(valor: String?): TipoActividad? =
        valor?.let(TipoActividad::valueOf)

    @TypeConverter
    fun prioridadAValor(prioridad: Prioridad?): String? = prioridad?.name

    @TypeConverter
    fun valorAPrioridad(valor: String?): Prioridad? = valor?.let(Prioridad::valueOf)

    @TypeConverter
    fun estadoTareaAValor(estado: EstadoTarea?): String? = estado?.name

    @TypeConverter
    fun valorAEstadoTarea(valor: String?): EstadoTarea? = valor?.let(EstadoTarea::valueOf)

    @TypeConverter
    fun recordatorioAValor(recordatorio: AnticipacionRecordatorio?): String? =
        recordatorio?.name

    @TypeConverter
    fun valorARecordatorio(valor: String?): AnticipacionRecordatorio? =
        valor?.let(AnticipacionRecordatorio::valueOf)

    @TypeConverter
    fun palabrasClaveAValor(palabras: List<String>?): String? =
        palabras?.joinToString(SEPARADOR_PALABRAS)

    @TypeConverter
    fun valorAPalabrasClave(valor: String?): List<String>? =
        valor?.takeIf(String::isNotEmpty)?.split(SEPARADOR_PALABRAS) ?: valor?.let { emptyList() }

    private companion object {
        const val SEPARADOR_PALABRAS = "\u001F"
    }
}
