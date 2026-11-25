
package com.santi.organizame

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ReceptorRecordatorio : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        Log.d("OrganizaMeAvisos", "El receptor recibió el recordatorio")

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("OrganizaMeAvisos", "Sin permiso de notificaciones")
            return
        }

        val titulo = intent.getStringExtra(EXTRA_TITULO) ?: "Actividad"
        val actividadId = intent.getLongExtra(EXTRA_ACTIVIDAD_ID, -1L)

        val tipo = when (intent.getStringExtra(EXTRA_TIPO)) {
            "EVENTO" -> "Evento"
            else -> "Tarea"
        }

        val fecha = intent.getStringExtra(EXTRA_FECHA)
            ?.let {
                runCatching {
                    LocalDate.parse(it).format(FORMATO_FECHA)
                }.getOrNull()
            }

        val hora = intent.getStringExtra(EXTRA_HORA)

        val detalle = listOfNotNull(
            tipo,
            fecha,
            hora?.let { "$it h" }
        ).joinToString(" · ")

        val abrirActividad = Intent(
            context,
            MainActivity::class.java
        ).apply {
            putExtra(EXTRA_ACTIVIDAD_ID, actividadId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val contenidoPendiente = PendingIntent.getActivity(
            context,
            ProgramadorRecordatorios.identificador(actividadId),
            abrirActividad,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val administrador = context.getSystemService(
            NotificationManager::class.java
        )

        crearCanal(administrador)

        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Recordatorio: $titulo")
            .setContentText(detalle)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(detalle)
            )
            .setContentIntent(contenidoPendiente)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .build()

        administrador.notify(
            ProgramadorRecordatorios.identificador(actividadId),
            notificacion
        )


// Iniciar la alarma continua
        val iniciarAlarma = Intent(
            context,
            ServicioAlarma::class.java
        ).apply {
            putExtra(EXTRA_TITULO, titulo)
            putExtra(EXTRA_ACTIVIDAD_ID, actividadId)
        }

        try {
            ContextCompat.startForegroundService(
                context,
                iniciarAlarma
            )

            Log.d(
                "OrganizaMeAvisos",
                "Inicio de alarma continua solicitado"
            )

        } catch (error: Exception) {

            Log.e(
                "OrganizaMeAvisos",
                "No se pudo iniciar la alarma continua",
                error
            )
        }


        Log.d(
            "OrganizaMeAvisos",
            "Notificación de alta prioridad enviada a Android"
        )
    }

    private fun crearCanal(administrador: NotificationManager) {

        val sonido = RingtoneManager.getDefaultUri(
            RingtoneManager.TYPE_NOTIFICATION
        ) ?: RingtoneManager.getDefaultUri(
            RingtoneManager.TYPE_ALARM
        )

        val atributosAudio = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
            .setContentType(
                AudioAttributes.CONTENT_TYPE_SONIFICATION
            )
            .build()

        val canal = NotificationChannel(
            CANAL_ID,
            "Recordatorios importantes",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {

            description =
                "Avisos importantes de tareas y eventos de OrganizaMe"

            if (sonido != null) {
                setSound(sonido, atributosAudio)
            }

            enableVibration(true)
            setVibrationPattern(
                longArrayOf(0, 250, 150, 250)
            )
        }

        administrador.createNotificationChannel(canal)
    }

    companion object {

        // Nuevo canal para evitar la configuración anterior
        const val CANAL_ID = "recordatorios_alerta_v2"

        const val EXTRA_ACTIVIDAD_ID = "actividadId"
        const val EXTRA_TITULO = "titulo"
        const val EXTRA_TIPO = "tipo"
        const val EXTRA_FECHA = "fecha"
        const val EXTRA_HORA = "hora"

        private val FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu")
    }
}
