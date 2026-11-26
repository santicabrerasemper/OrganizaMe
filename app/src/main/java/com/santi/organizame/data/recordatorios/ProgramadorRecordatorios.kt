package com.santi.organizame.data.recordatorios

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.AnticipacionRecordatorio
import com.santi.organizame.modelo.EstadoTarea
import com.santi.organizame.domain.recordatorios.CalculadorMomentoRecordatorio
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class ProgramadorRecordatorios(private val contexto: Context) {

    enum class ResultadoProgramacion {
        EXACTO,
        APROXIMADO,
        NO_PROGRAMADO
    }

    private val administrador =
        contexto.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun programar(actividad: Actividad): ResultadoProgramacion {
        cancelar(actividad.id)

        if (actividad.estadoTarea == EstadoTarea.COMPLETADA) {
            return ResultadoProgramacion.NO_PROGRAMADO
        }

        val momentoAviso = calcularMomentoAviso(
            actividad.fecha,
            actividad.hora,
            actividad.recordatorio
        ) ?: return ResultadoProgramacion.NO_PROGRAMADO

        if (!momentoAviso.isAfter(Instant.now())) {
            Log.d("OrganizaMeAvisos", "La hora del recordatorio ya pasó")
            return ResultadoProgramacion.NO_PROGRAMADO
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

        val resultado = if (puedeProgramarAlarmasExactas()) {
            administrador.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                momentoAviso.toEpochMilli(),
                pendiente
            )
            ResultadoProgramacion.EXACTO
        } else {
            administrador.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                momentoAviso.toEpochMilli(),
                pendiente
            )
            ResultadoProgramacion.APROXIMADO
        }

        Log.d(
            "OrganizaMeAvisos",
            "Recordatorio programado (${resultado.name.lowercase()}): ${actividad.titulo}"
        )
        return resultado
    }

    fun puedeProgramarAlarmasExactas(): Boolean =
        debeUsarAlarmaExacta(
            sdkInt = Build.VERSION.SDK_INT,
            permisoConcedido = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                administrador.canScheduleExactAlarms()
        )

    fun solicitarAccesoAlarmasExactas(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            administrador.canScheduleExactAlarms()
        ) {
            return false
        }

        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:${contexto.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return runCatching {
            contexto.startActivity(intent)
            true
        }.onFailure { error ->
            Log.e(
                "OrganizaMeAvisos",
                "No se pudo abrir la configuración de alarmas exactas",
                error
            )
        }.getOrDefault(false)
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
        fun debeUsarAlarmaExacta(
            sdkInt: Int,
            permisoConcedido: Boolean
        ): Boolean = sdkInt < Build.VERSION_CODES.S || permisoConcedido

        fun calcularMomentoAviso(
            fecha: LocalDate?,
            hora: LocalTime?,
            recordatorio: AnticipacionRecordatorio?,
            zona: ZoneId = ZoneId.systemDefault()
        ): Instant? = CalculadorMomentoRecordatorio.calcular(
            fecha = fecha,
            hora = hora,
            recordatorio = recordatorio,
            zona = zona
        )

        fun identificador(id: Long): Int = id.hashCode()
    }
}
