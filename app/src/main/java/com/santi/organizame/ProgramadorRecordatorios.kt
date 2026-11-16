package com.santi.organizame

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.AnticipacionRecordatorio
import com.santi.organizame.modelo.EstadoTarea
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class ProgramadorRecordatorios(private val contexto: Context) {

    private val administrador =
        contexto.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun programar(actividad: Actividad) {
        cancelar(actividad.id)

        if (actividad.estadoTarea == EstadoTarea.COMPLETADA) return

        val momentoAviso = calcularMomentoAviso(
            actividad.fecha,
            actividad.hora,
            actividad.recordatorio
        ) ?: return

        if (!momentoAviso.isAfter(Instant.now())) {
            Log.d("OrganizaMeAvisos", "La hora del recordatorio ya pasó")
            return
        }

        val intent = Intent(contexto, ReceptorRecordatorio::class.java).apply {
            putExtra(ReceptorRecordatorio.EXTRA_TITULO, actividad.titulo)
            putExtra(ReceptorRecordatorio.EXTRA_TIPO, actividad.tipo.name)
            putExtra(ReceptorRecordatorio.EXTRA_FECHA, actividad.fecha.toString())
            putExtra(ReceptorRecordatorio.EXTRA_HORA, actividad.hora.toString())
            putExtra(ReceptorRecordatorio.EXTRA_ACTIVIDAD_ID, actividad.id)
        }

        val pendiente = PendingIntent.getBroadcast(
            contexto,
            identificador(actividad.id),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        administrador.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            momentoAviso.toEpochMilli(),
            pendiente
        )
        Log.d("OrganizaMeAvisos", "Recordatorio programado: ${actividad.titulo}")
    }

    fun cancelar(id: Long) {
        val intent = Intent(contexto, ReceptorRecordatorio::class.java)

        val pendiente = PendingIntent.getBroadcast(
            contexto,
            identificador(id),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendiente != null) {
            administrador.cancel(pendiente)
            pendiente.cancel()
        }

        contexto.getSystemService(NotificationManager::class.java)
            .cancel(identificador(id))
    }

    companion object {
        fun calcularMomentoAviso(
            fecha: LocalDate?,
            hora: LocalTime?,
            recordatorio: AnticipacionRecordatorio?,
            zona: ZoneId = ZoneId.systemDefault()
        ): Instant? {
            if (fecha == null || hora == null || recordatorio == null) return null

            val minutosAntes = when (recordatorio) {
                AnticipacionRecordatorio.A_LA_HORA -> 0L
                AnticipacionRecordatorio.DIEZ_MINUTOS_ANTES -> 10L
                AnticipacionRecordatorio.TREINTA_MINUTOS_ANTES -> 30L
                AnticipacionRecordatorio.UNA_HORA_ANTES -> 60L
                AnticipacionRecordatorio.UN_DIA_ANTES -> 1440L
            }

            return LocalDateTime.of(fecha, hora)
                .minusMinutes(minutosAntes)
                .atZone(zona)
                .toInstant()
        }

        fun identificador(id: Long): Int = id.hashCode()
    }
}
