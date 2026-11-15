package com.santi.organizame

import android.Manifest
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import android.util.Log

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

        val titulo = intent.getStringExtra("titulo") ?: "Actividad"
        val actividadId = intent.getIntExtra("actividadId", 0)

        val notificacion = NotificationCompat.Builder(context, "recordatorios")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Recordatorio: $titulo")
            .setContentText("Tenés una actividad programada.")
            .setAutoCancel(true)
            .build()

        val administrador =
            context.getSystemService(NotificationManager::class.java)

        administrador.notify(actividadId, notificacion)
        Log.d("OrganizaMeAvisos", "Notificación enviada a Android")
        Log.d(
            "OrganizaMeAvisos",
            "Notificaciones activas: ${administrador.activeNotifications.size}"
        )
    }
}