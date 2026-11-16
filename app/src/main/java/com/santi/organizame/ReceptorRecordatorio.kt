package com.santi.organizame

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import android.util.Log
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ReceptorRecordatorio : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("OrganizaMeAvisos", "El receptor recibió el recordatorio")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val titulo = intent.getStringExtra(EXTRA_TITULO) ?: "Actividad"
        val actividadId = intent.getLongExtra(EXTRA_ACTIVIDAD_ID, -1L)
        val tipo = when (intent.getStringExtra(EXTRA_TIPO)) {
            "EVENTO" -> "Evento"
            else -> "Tarea"
        }
        val fecha = intent.getStringExtra(EXTRA_FECHA)
            ?.let { runCatching { LocalDate.parse(it).format(FORMATO_FECHA) }.getOrNull() }
        val hora = intent.getStringExtra(EXTRA_HORA)
        val detalle = listOfNotNull(tipo, fecha, hora?.let { "$it h" })
            .joinToString(" · ")

        val abrirActividad = Intent(context, MainActivity::class.java).apply {
            putExtra(EXTRA_ACTIVIDAD_ID, actividadId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contenidoPendiente = PendingIntent.getActivity(
            context,
            ProgramadorRecordatorios.identificador(actividadId),
            abrirActividad,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val administrador = context.getSystemService(NotificationManager::class.java)
        crearCanal(administrador)

        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Recordatorio: $titulo")
            .setContentText(detalle)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detalle))
            .setContentIntent(contenidoPendiente)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        administrador.notify(
            ProgramadorRecordatorios.identificador(actividadId),
            notificacion
        )
        Log.d("OrganizaMeAvisos", "Notificación enviada a Android")
    }

    private fun crearCanal(administrador: NotificationManager) {
        val canal = NotificationChannel(
            CANAL_ID,
            "Recordatorios de actividades",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Avisos de las tareas y eventos de OrganizaMe"
        }
        administrador.createNotificationChannel(canal)
    }

    companion object {
        const val CANAL_ID = "recordatorios"
        const val EXTRA_ACTIVIDAD_ID = "actividadId"
        const val EXTRA_TITULO = "titulo"
        const val EXTRA_TIPO = "tipo"
        const val EXTRA_FECHA = "fecha"
        const val EXTRA_HORA = "hora"

        private val FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/uuuu")
    }
}
