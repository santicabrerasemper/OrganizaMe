package com.santi.organizame

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.AlarmManager
import android.util.Log
import com.santi.organizame.data.recordatorios.GestorRecordatoriosAndroid
import com.santi.organizame.data.repository.RoomActividadRepository
import com.santi.organizame.domain.usecase.ReprogramarRecordatoriosUseCase
import com.santi.organizame.modelo.OrganizaMeDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ReceptorReprogramacion : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED &&
            intent.action != AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
        ) {
            return
        }

        val resultadoPendiente = goAsync()
        val repositorio = RoomActividadRepository(
            OrganizaMeDatabase.obtener(context).actividadDao()
        )
        val reprogramar = ReprogramarRecordatoriosUseCase(
            repositorio,
            GestorRecordatoriosAndroid(
                ProgramadorRecordatorios(context.applicationContext)
            )
        )

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                reprogramar()
            } catch (error: Throwable) {
                Log.e(
                    "OrganizaMeAvisos",
                    "No se pudieron restaurar los recordatorios",
                    error
                )
            } finally {
                resultadoPendiente.finish()
            }
        }
    }
}
