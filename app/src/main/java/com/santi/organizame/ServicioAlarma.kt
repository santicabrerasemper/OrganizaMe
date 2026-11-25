
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

        const val ACCION_FINALIZADA =
            "com.santi.organizame.ALARMA_FINALIZADA"

        private const val CANAL_ID = "alarmas_continuas_v2"
        private const val ID_NOTIFICACION = 900001
        private const val DURACION_MAXIMA = 120000L
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        // Apagar alarma desde la notificación
        if (intent?.action == ACCION_APAGAR) {

            Log.d(
                "OrganizaMeAvisos",
                "Usuario apagó la alarma"
            )

            stopSelf()
            return START_NOT_STICKY
        }

        // Obtener datos del recordatorio
        val titulo = intent?.getStringExtra(
            ReceptorRecordatorio.EXTRA_TITULO
        ) ?: "Actividad pendiente"

        crearCanal()

        // Botón Apagar alarma de la notificación
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

        // Preparar la pantalla completa de alarma
        val intentPantalla = Intent(
            this,
            PantallaAlarmaActividad::class.java
        ).apply {

            putExtra(
                ReceptorRecordatorio.EXTRA_TITULO,
                titulo
            )

            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendientePantalla = PendingIntent.getActivity(
            this,
            900002,
            intentPantalla,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        // Notificación de alarma activa
        val notificacion = NotificationCompat.Builder(
            this,
            CANAL_ID
        )
            .setSmallIcon(
                android.R.drawable.ic_dialog_alert
            )
            .setContentTitle("Alarma: $titulo")
            .setContentText("La alarma está sonando")

            // Tocar la notificación abre la pantalla
            .setContentIntent(pendientePantalla)

            .setCategory(
                NotificationCompat.CATEGORY_ALARM
            )

            .setPriority(
                NotificationCompat.PRIORITY_HIGH
            )

            // Solicitar pantalla completa
            .setFullScreenIntent(
                pendientePantalla,
                true
            )

            .setOngoing(true)
            .setOnlyAlertOnce(true)

            // Mantener botón en la barra de notificaciones
            .addAction(
                android.R.drawable.ic_media_pause,
                "Apagar alarma",
                pendienteApagar
            )

            .build()

        // Mantener servicio activo mientras suena
        try {

            startForeground(
                ID_NOTIFICACION,
                notificacion
            )

        } catch (error: Exception) {

            Log.e(
                "OrganizaMeAvisos",
                "No se pudo iniciar el servicio",
                error
            )

            stopSelf()
            return START_NOT_STICKY
        }

        // Reproducir el sonido continuamente
        iniciarSonido()

        // Apagado automático después de dos minutos
        manejador.removeCallbacks(apagadoAutomatico)

        manejador.postDelayed(
            apagadoAutomatico,
            DURACION_MAXIMA
        )

        return START_NOT_STICKY
    }

    // Canal especial para alarmas
    private fun crearCanal() {

        val administrador = getSystemService(
            NotificationManager::class.java
        )

        val canal = NotificationChannel(
            CANAL_ID,
            "Alarmas activas",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {

            description =
                "Alarmas con opción para apagar"

            // El sonido lo reproduce MediaPlayer
            setSound(null, null)
        }

        administrador.createNotificationChannel(canal)
    }

    // Reproducir sonido de alarma
    private fun iniciarSonido() {

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
                        .setUsage(
                            AudioAttributes.USAGE_ALARM
                        )
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

    // Liberar recursos cuando termina la alarma
    override fun onDestroy() {

        manejador.removeCallbacks(apagadoAutomatico)

        reproductor?.release()
        reproductor = null

        stopForeground(STOP_FOREGROUND_REMOVE)

        Log.d(
            "OrganizaMeAvisos",
            "Servicio de alarma detenido"
        )

        // Avisar a la pantalla que la alarma terminó
        sendBroadcast(
            Intent(ACCION_FINALIZADA).setPackage(packageName)
        )

        super.onDestroy()
    }
}
