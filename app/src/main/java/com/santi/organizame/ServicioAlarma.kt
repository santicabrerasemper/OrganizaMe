
package com.santi.organizame

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat

class ServicioAlarma : Service() {

    private var reproductor: MediaPlayer? = null
    private val manejador = Handler(Looper.getMainLooper())

    private val apagadoAutomatico = Runnable {
        Log.d("OrganizaMeAvisos", "Alarma detenida por tiempo")
        stopSelf()
    }

    companion object {
        const val ACCION_APAGAR =
            "com.santi.organizame.APAGAR_ALARMA"

        private const val CANAL_ID = "alarmas_continuas_v1"
        private const val ID_NOTIFICACION = 900001
        private const val DURACION_MAXIMA = 120000L
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        // Detener cuando el usuario toca Apagar alarma
        if (intent?.action == ACCION_APAGAR) {
            Log.d("OrganizaMeAvisos", "Usuario apagó la alarma")
            stopSelf()
            return START_NOT_STICKY
        }

        val titulo = intent?.getStringExtra(
            ReceptorRecordatorio.EXTRA_TITULO
        ) ?: "Actividad pendiente"

        val actividadId = intent?.getLongExtra(
            ReceptorRecordatorio.EXTRA_ACTIVIDAD_ID,
            -1L
        ) ?: -1L

        crearCanal()

        // Acción para apagar el sonido
        val intentApagar = Intent(
            this,
            ServicioAlarma::class.java
        ).apply {
            action = ACCION_APAGAR
        }

        val pendienteApagar = PendingIntent.getService(
            this,
            900001,
            intentApagar,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        // Abrir la actividad al tocar la notificación
        val intentAbrir = Intent(
            this,
            MainActivity::class.java
        ).apply {
            putExtra(
                ReceptorRecordatorio.EXTRA_ACTIVIDAD_ID,
                actividadId
            )
            this.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendienteAbrir = PendingIntent.getActivity(
            this,
            900001,
            intentAbrir,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        // Notificación permanente mientras suena
        val notificacion = NotificationCompat.Builder(
            this,
            CANAL_ID
        )
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Alarma: $titulo")
            .setContentText("La alarma está sonando")
            .setContentIntent(pendienteAbrir)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(
                android.R.drawable.ic_media_pause,
                "Apagar alarma",
                pendienteApagar
            )
            .build()

        // Mantener el servicio activo
        try {
            startForeground(ID_NOTIFICACION, notificacion)
        } catch (error: Exception) {
            Log.e(
                "OrganizaMeAvisos",
                "No se pudo iniciar el servicio de alarma",
                error
            )
            stopSelf()
            return START_NOT_STICKY
        }

        iniciarSonido()

        // Detener automáticamente después de dos minutos
        manejador.removeCallbacks(apagadoAutomatico)
        manejador.postDelayed(
            apagadoAutomatico,
            DURACION_MAXIMA
        )

        return START_NOT_STICKY
    }

    private fun crearCanal() {

        val administrador = getSystemService(
            NotificationManager::class.java
        )

        val canal = NotificationChannel(
            CANAL_ID,
            "Alarmas activas",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Alarmas con opción para apagar"
            setSound(null, null)
        }

        administrador.createNotificationChannel(canal)
    }

    private fun iniciarSonido() {

        // Detener un sonido anterior si existiera
        reproductor?.release()
        reproductor = null

        val sonidos = listOfNotNull(
            RingtoneManager.getActualDefaultRingtoneUri(
                this,
                RingtoneManager.TYPE_ALARM
            ),
            RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_ALARM
            ),
            RingtoneManager.getActualDefaultRingtoneUri(
                this,
                RingtoneManager.TYPE_NOTIFICATION
            )
        ).distinct()

        for (sonido in sonidos) {

            val nuevoReproductor = MediaPlayer()

            try {
                nuevoReproductor.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(
                            AudioAttributes.CONTENT_TYPE_SONIFICATION
                        )
                        .build()
                )

                nuevoReproductor.setDataSource(
                    applicationContext,
                    sonido
                )

                nuevoReproductor.isLooping = true
                nuevoReproductor.prepare()
                nuevoReproductor.start()

                reproductor = nuevoReproductor

                Log.d(
                    "OrganizaMeAvisos",
                    "Alarma continua sonando"
                )

                return

            } catch (error: Exception) {

                Log.e(
                    "OrganizaMeAvisos",
                    "No se pudo reproducir el sonido",
                    error
                )

                nuevoReproductor.release()
            }
        }

        Log.e(
            "OrganizaMeAvisos",
            "No se encontró un sonido disponible"
        )
    }

    override fun onDestroy() {

        manejador.removeCallbacks(apagadoAutomatico)

        reproductor?.release()
        reproductor = null

        stopForeground(STOP_FOREGROUND_REMOVE)

        Log.d("OrganizaMeAvisos", "Servicio de alarma detenido")

        super.onDestroy()
    }
}
