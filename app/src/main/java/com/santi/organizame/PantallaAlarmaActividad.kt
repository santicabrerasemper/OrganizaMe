
package com.santi.organizame

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class PantallaAlarmaActividad : AppCompatActivity() {

    private val receptorCierre = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context?,
            intent: Intent?
        ) {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setShowWhenLocked(true)
        setTurnScreenOn(true)

        setContentView(R.layout.activity_pantalla_alarma)

        val titulo = intent.getStringExtra(
            ReceptorRecordatorio.EXTRA_TITULO
        ) ?: "Actividad pendiente"

        findViewById<TextView>(
            R.id.textoTituloAlarma
        ).text = titulo

        findViewById<Button>(
            R.id.btnApagarPantallaAlarma
        ).setOnClickListener {
            apagarAlarma()
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    apagarAlarma()
                }
            }
        )
    }

    private fun apagarAlarma() {
        stopService(
            Intent(this, ServicioAlarma::class.java)
        )
        finish()
    }

    override fun onStart() {
        super.onStart()

        ContextCompat.registerReceiver(
            this,
            receptorCierre,
            IntentFilter(ServicioAlarma.ACCION_FINALIZADA),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onStop() {
        unregisterReceiver(receptorCierre)
        super.onStop()
    }
}
