package com.santi.organizame

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.widget.Button
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.AnticipacionRecordatorio
import com.santi.organizame.modelo.EstadoTarea
import com.santi.organizame.modelo.OrganizaMeDatabase
import com.santi.organizame.modelo.TipoActividad
import com.santi.organizame.data.recordatorios.ProgramadorRecordatorios
import com.santi.organizame.data.recordatorios.ReceptorRecordatorio
import com.santi.organizame.data.recordatorios.ServicioAlarma
import com.santi.organizame.ui.alarma.PantallaAlarmaActividad
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class RecordatorioE2EInstrumentadoTest {

    private lateinit var contexto: Context
    private lateinit var baseDeDatos: OrganizaMeDatabase
    private lateinit var notificaciones: NotificationManager
    private var actividadId: Long = -1L

    @Before
    fun preparar() {
        contexto = ApplicationProvider.getApplicationContext()
        baseDeDatos = OrganizaMeDatabase.obtener(contexto)
        baseDeDatos.clearAllTables()
        notificaciones = contexto.getSystemService(NotificationManager::class.java)
        notificaciones.cancelAll()

        val automatizacion = InstrumentationRegistry.getInstrumentation().uiAutomation
        automatizacion.executeShellCommand(
            "pm grant ${contexto.packageName} android.permission.POST_NOTIFICATIONS"
        ).close()
        automatizacion.executeShellCommand(
            "appops set ${contexto.packageName} SCHEDULE_EXACT_ALARM allow"
        ).close()
    }

    @After
    fun limpiar() {
        if (actividadId > 0) ProgramadorRecordatorios(contexto).cancelar(actividadId)
        contexto.stopService(Intent(contexto, ServicioAlarma::class.java))
        notificaciones.cancelAll()
        baseDeDatos.clearAllTables()
    }

    @Test
    fun recordatorioRealDisparaNotificacionAlarmaYPuedeApagarse() {
        val momento = LocalDateTime.now().plusSeconds(8)
        actividadId = baseDeDatos.actividadDao().insertar(
            Actividad(
                tipo = TipoActividad.TAREA,
                titulo = "Recordatorio E2E",
                fecha = momento.toLocalDate(),
                hora = momento.toLocalTime(),
                estadoTarea = EstadoTarea.PENDIENTE,
                recordatorio = AnticipacionRecordatorio.A_LA_HORA
            )
        )
        val guardada = checkNotNull(baseDeDatos.actividadDao().obtenerPorId(actividadId))

        assertEquals(
            ProgramadorRecordatorios.ResultadoProgramacion.EXACTO,
            ProgramadorRecordatorios(contexto).programar(guardada)
        )

        val idNotificacion = ProgramadorRecordatorios.identificador(actividadId)
        esperar(30_000) {
            notificaciones.activeNotifications.any { it.id == idNotificacion }
        }
        val recordatorio = notificaciones.activeNotifications.first { it.id == idNotificacion }
        assertEquals(
            "Recordatorio: Recordatorio E2E",
            recordatorio.notification.extras
                .getCharSequence(Notification.EXTRA_TITLE)
                .toString()
        )
        esperar(10_000) {
            notificaciones.activeNotifications.any {
                it.notification.extras
                    .getCharSequence(Notification.EXTRA_TITLE)
                    ?.toString() == "Alarma: Recordatorio E2E"
            }
        }

        val pantalla = ActivityScenario.launch<PantallaAlarmaActividad>(
            Intent(contexto, PantallaAlarmaActividad::class.java).apply {
                putExtra(ReceptorRecordatorio.EXTRA_TITULO, "Recordatorio E2E")
            }
        )
        pantalla.onActivity { actividad ->
            assertEquals(
                "Recordatorio E2E",
                actividad.findViewById<TextView>(R.id.textoTituloAlarma).text.toString()
            )
            actividad.findViewById<Button>(R.id.btnApagarPantallaAlarma).performClick()
        }
        esperar(5_000) {
            notificaciones.activeNotifications.none {
                it.notification.extras
                    .getCharSequence(Notification.EXTRA_TITLE)
                    ?.toString() == "Alarma: Recordatorio E2E"
            }
        }
        runCatching { pantalla.close() }
    }

    private fun esperar(tiempoMaximoMs: Long, condicion: () -> Boolean) {
        val limite = System.currentTimeMillis() + tiempoMaximoMs
        while (System.currentTimeMillis() < limite) {
            if (condicion()) return
            Thread.sleep(100)
        }
        assertTrue("La condición no se cumplió dentro del tiempo esperado", condicion())
    }
}
