package com.santi.organizame

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.AnticipacionRecordatorio
import java.time.LocalDateTime
import android.util.Log
import java.time.ZoneId

class ProgramadorRecordatorios(private val contexto: Context) {

    private val administrador =
        contexto.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun programar(actividad: Actividad) {

        cancelar(actividad.id)

        val fecha = actividad.fecha ?: return
        val hora = actividad.hora ?: return
        val recordatorio = actividad.recordatorio ?: return

        val minutosAntes = when (recordatorio) {
            AnticipacionRecordatorio.A_LA_HORA -> 0L
            AnticipacionRecordatorio.DIEZ_MINUTOS_ANTES -> 10L
            AnticipacionRecordatorio.TREINTA_MINUTOS_ANTES -> 30L
            AnticipacionRecordatorio.UNA_HORA_ANTES -> 60L
            AnticipacionRecordatorio.UN_DIA_ANTES -> 1440L
        }

        val momentoAviso = LocalDateTime.of(fecha, hora)
            .minusMinutes(minutosAntes)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        if (momentoAviso <= System.currentTimeMillis()) {
            Log.d("OrganizaMeAvisos", "La hora del recordatorio ya pasó")
            return
        }

        val intent = Intent(contexto, ReceptorRecordatorio::class.java).apply {
            putExtra("titulo", actividad.titulo)
            putExtra("actividadId", actividad.id.toInt())
        }

        val pendiente = PendingIntent.getBroadcast(
            contexto,
            actividad.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        administrador.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            momentoAviso,
            pendiente
        )
        Log.d("OrganizaMeAvisos", "Recordatorio programado: ${actividad.titulo}")
    }

    fun cancelar(id: Long) {

        val intent = Intent(contexto, ReceptorRecordatorio::class.java)

        val pendiente = PendingIntent.getBroadcast(
            contexto,
            id.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendiente != null) {
            administrador.cancel(pendiente)
            pendiente.cancel()
        }
    }
}