package com.santi.organizame

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.santi.organizame.modelo.ActividadRepository
import com.santi.organizame.modelo.OrganizaMeDatabase

class ReceptorReprogramacion : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }

        val resultadoPendiente = goAsync()
        val repositorio = ActividadRepository(
            OrganizaMeDatabase.obtener(context).actividadDao()
        )

        repositorio.obtenerTodas { resultado ->
            try {
                val programador = ProgramadorRecordatorios(context.applicationContext)
                resultado
                    .onSuccess { actividades ->
                        actividades.forEach(programador::programar)
                    }
                    .onFailure { error ->
                        Log.e(
                            "OrganizaMeAvisos",
                            "No se pudieron restaurar los recordatorios",
                            error
                        )
                    }
            } finally {
                resultadoPendiente.finish()
            }
        }
    }
}
