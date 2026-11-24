package com.santi.organizame

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.Categoria
import com.santi.organizame.modelo.EstadoTarea
import com.santi.organizame.modelo.OrganizaMeDatabase
import com.santi.organizame.modelo.Prioridad
import com.santi.organizame.modelo.Recurrencia
import com.santi.organizame.modelo.TipoActividad
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@RunWith(AndroidJUnit4::class)
class FlujosAceptacionUiInstrumentadoTest {

    private lateinit var contexto: Context
    private lateinit var baseDeDatos: OrganizaMeDatabase
    private var categoriaId: Long = -1L

    @Before
    fun prepararDatos() {
        contexto = ApplicationProvider.getApplicationContext()
        baseDeDatos = OrganizaMeDatabase.obtener(contexto)
        baseDeDatos.clearAllTables()
        categoriaId = baseDeDatos.categoriaDao().insertar(
            Categoria(
                nombre = "Universidad",
                color = "#7C3AED",
                palabrasClave = listOf("facultad", "parcial")
            )
        )

        val instrumentacion = InstrumentationRegistry.getInstrumentation()
        instrumentacion.uiAutomation.executeShellCommand(
            "pm grant ${contexto.packageName} android.permission.POST_NOTIFICATIONS"
        ).close()
        instrumentacion.uiAutomation.executeShellCommand(
            "appops set ${contexto.packageName} SCHEDULE_EXACT_ALARM allow"
        ).close()
    }

    @After
    fun limpiarDatos() {
        baseDeDatos.actividadDao().obtenerTodas().forEach {
            ProgramadorRecordatorios(contexto).cancelar(it.id)
        }
        baseDeDatos.clearAllTables()
    }

    @Test
    fun altaManualYEdicionPersistenTodosLosCampos() {
        val fechaInicial = LocalDate.now().plusDays(2)
        val escenario = ActivityScenario.launch<NuevaTareaActividad>(
            Intent(contexto, NuevaTareaActividad::class.java)
        )

        esperar { botonHabilitado(escenario, R.id.btnGuardarTarea) }
        escenario.onActivity { actividad ->
            actividad.findViewById<Spinner>(R.id.tipoActividad).setSelection(0)
            actividad.findViewById<EditText>(R.id.tituloTarea).setText("Álgebra final")
            actividad.findViewById<EditText>(R.id.descripcionTarea)
                .setText("Resolver guía completa")
            actividad.findViewById<EditText>(R.id.fechaTarea)
                .setText(fechaInicial.format(FORMATO_FECHA))
            actividad.findViewById<EditText>(R.id.horaTarea).setText("18:00")
            actividad.findViewById<Spinner>(R.id.prioridadTarea)
                .setSelection(Prioridad.values().indexOf(Prioridad.ALTA))
            actividad.findViewById<Spinner>(R.id.categoriaTarea).setSelection(1)
            actividad.findViewById<Spinner>(R.id.recurrenciaTarea).setSelection(1)
            actividad.findViewById<Button>(R.id.btnGuardarTarea).performClick()
        }

        esperar { baseDeDatos.actividadDao().obtenerTodas().size == 1 }
        val creada = baseDeDatos.actividadDao().obtenerTodas().single()
        assertEquals("Álgebra final", creada.titulo)
        assertEquals("Resolver guía completa", creada.descripcion)
        assertEquals(fechaInicial, creada.fecha)
        assertEquals(LocalTime.of(18, 0), creada.hora)
        assertEquals(Prioridad.ALTA, creada.prioridad)
        assertEquals(categoriaId, creada.categoriaId)
        assertEquals(Recurrencia.DIARIA, creada.recurrencia)
        assertEquals(EstadoTarea.PENDIENTE, creada.estadoTarea)

        val editar = Intent(contexto, NuevaTareaActividad::class.java).apply {
            putExtra("actividadId", creada.id)
        }
        val escenarioEdicion = ActivityScenario.launch<NuevaTareaActividad>(editar)
        esperar {
            textoVista(escenarioEdicion, R.id.tituloTarea) == "Álgebra final" &&
                botonHabilitado(escenarioEdicion, R.id.btnGuardarTarea)
        }
        escenarioEdicion.onActivity { actividad ->
            actividad.findViewById<EditText>(R.id.tituloTarea).setText("Álgebra avanzada")
            actividad.findViewById<EditText>(R.id.horaTarea).setText("19:15")
            actividad.findViewById<Spinner>(R.id.recurrenciaTarea).setSelection(2)
            actividad.findViewById<Button>(R.id.btnGuardarTarea).performClick()
        }

        esperar {
            baseDeDatos.actividadDao().obtenerPorId(creada.id)?.titulo == "Álgebra avanzada"
        }
        val editada = checkNotNull(baseDeDatos.actividadDao().obtenerPorId(creada.id))
        assertEquals(LocalTime.of(19, 15), editada.hora)
        assertEquals(Recurrencia.SEMANAL, editada.recurrencia)
        assertEquals(1, baseDeDatos.actividadDao().obtenerTodas().size)

        runCatching { escenario.close() }
        runCatching { escenarioEdicion.close() }
    }

    @Test
    fun parserCargaUnaVistaPreviaCompletaYEditable() {
        val instrumentacion = InstrumentationRegistry.getInstrumentation()
        val monitor = instrumentacion.addMonitor(
            NuevaTareaActividad::class.java.name,
            null,
            false
        )
        val escenario = ActivityScenario.launch(MainActivity::class.java)
        escenario.onActivity { actividad ->
            actividad.findViewById<EditText>(R.id.entradaRapida).setText(
                "reunión mañana a las 18:30 prioridad alta facultad " +
                    "descripción llevar presentación recordatorio 10 minutos antes"
            )
            actividad.findViewById<Button>(R.id.btnInterpretar).performClick()
        }

        val formulario = instrumentacion.waitForMonitorWithTimeout(monitor, 5_000)
        assertNotNull("El parser no abrió la pantalla de revisión", formulario)
        esperar {
            var categoriaSeleccionada = ""
            instrumentacion.runOnMainSync {
                categoriaSeleccionada = checkNotNull(formulario)
                    .findViewById<Spinner>(R.id.categoriaTarea)
                    .selectedItem
                    ?.toString()
                    .orEmpty()
            }
            categoriaSeleccionada == "Universidad"
        }
        instrumentacion.runOnMainSync {
            checkNotNull(formulario)
            assertEquals("Revisar interpretación", formulario.findViewById<TextView>(R.id.tituloFormulario).text)
            assertEquals("reunión", formulario.findViewById<EditText>(R.id.tituloTarea).text.toString())
            assertEquals(
                "llevar presentación",
                formulario.findViewById<EditText>(R.id.descripcionTarea).text.toString()
            )
            assertEquals(
                LocalDate.now().plusDays(1).format(FORMATO_FECHA),
                formulario.findViewById<EditText>(R.id.fechaTarea).text.toString()
            )
            assertEquals("18:30", formulario.findViewById<EditText>(R.id.horaTarea).text.toString())
            assertEquals("Evento", formulario.findViewById<Spinner>(R.id.tipoActividad).selectedItem)
            assertEquals("ALTA", formulario.findViewById<Spinner>(R.id.prioridadTarea).selectedItem)
            assertEquals("Universidad", formulario.findViewById<Spinner>(R.id.categoriaTarea).selectedItem)
            assertEquals(
                "10 minutos antes",
                formulario.findViewById<Spinner>(R.id.recordatorioTarea).selectedItem
            )
            formulario.finish()
        }
        instrumentacion.removeMonitor(monitor)
        escenario.close()
    }

    @Test
    fun listadoBusquedaFiltrosRecurrenciaHoyCalendarioYResumen() {
        val hoy = LocalDate.now()
        val algebraId = insertar(
            titulo = "Álgebra",
            fecha = hoy.plusDays(2),
            prioridad = Prioridad.ALTA
        )
        insertar(
            titulo = "Defensa",
            fecha = hoy.plusDays(1),
            tipo = TipoActividad.EVENTO,
            prioridad = Prioridad.MEDIA
        )
        val rutinaId = insertar(
            titulo = "Rutina",
            fecha = hoy,
            prioridad = Prioridad.ALTA,
            recurrencia = Recurrencia.DIARIA
        )

        val listado = ActivityScenario.launch(ListaActividadesActividad::class.java)
        esperar {
            textoVista(listado, R.id.btnPendientes).contains("(2)") &&
                textoVista(listado, R.id.btnEventos).contains("(1)")
        }

        listado.onActivity { actividad ->
            actividad.findViewById<EditText>(R.id.buscarTitulo).setText("algebra")
        }
        esperar {
            textoVista(listado, R.id.btnPendientes).contains("(1)") &&
                visibilidadVista(listado, R.id.listaPendientes) == View.VISIBLE
        }
        assertTrue(textosVista(listado, R.id.listaPendientes).contains("Álgebra"))

        listado.onActivity { actividad ->
            actividad.findViewById<Spinner>(R.id.filtroCategoria).setSelection(2)
            actividad.findViewById<Spinner>(R.id.filtroPrioridad).setSelection(1)
            actividad.findViewById<Spinner>(R.id.filtroTipo).setSelection(1)
        }
        esperar { textoVista(listado, R.id.btnPendientes).contains("(1)") }
        listado.onActivity { actividad ->
            actividad.findViewById<Button>(R.id.btnMostrarTodas).performClick()
        }
        esperar { textoVista(listado, R.id.btnPendientes).contains("(2)") }

        listado.onActivity { actividad ->
            actividad.findViewById<EditText>(R.id.buscarTitulo).setText("Rutina")
        }
        esperar { visibilidadVista(listado, R.id.listaPendientes) == View.VISIBLE }
        pulsarAccionPrimeraTarjeta(listado, R.id.listaPendientes, R.id.btnCompletarActividad)

        esperar {
            val actividades = baseDeDatos.actividadDao().obtenerTodas()
            actividades.size == 4 &&
                actividades.first { it.id == rutinaId }.estadoTarea == EstadoTarea.COMPLETADA &&
                actividades.any {
                    it.id != rutinaId && it.titulo == "Rutina" &&
                        it.estadoTarea == EstadoTarea.PENDIENTE && it.fecha == hoy.plusDays(1)
                }
        }
        val totalTrasPrimeraFinalizacion = baseDeDatos.actividadDao().obtenerTodas().size

        esperar { visibilidadVista(listado, R.id.listaCompletadas) == View.VISIBLE }
        pulsarAccionPrimeraTarjeta(listado, R.id.listaCompletadas, R.id.btnCompletarActividad)
        esperar {
            baseDeDatos.actividadDao().obtenerPorId(rutinaId)?.estadoTarea == EstadoTarea.PENDIENTE
        }
        esperar { visibilidadVista(listado, R.id.listaPendientes) == View.VISIBLE }
        pulsarAccionPrimeraTarjeta(listado, R.id.listaPendientes, R.id.btnCompletarActividad)
        esperar {
            baseDeDatos.actividadDao().obtenerPorId(rutinaId)?.estadoTarea == EstadoTarea.COMPLETADA
        }
        assertEquals(totalTrasPrimeraFinalizacion, baseDeDatos.actividadDao().obtenerTodas().size)
        assertEquals(Prioridad.ALTA, baseDeDatos.actividadDao().obtenerPorId(algebraId)?.prioridad)
        listado.close()

        val hoyEscenario = ActivityScenario.launch(HoyActividad::class.java)
        esperar { textosVista(hoyEscenario, R.id.pantallaHoy).contains("Rutina") }
        assertTrue(textosVista(hoyEscenario, R.id.pantallaHoy).contains("Tarea completada"))
        hoyEscenario.close()

        val calendario = ActivityScenario.launch(CalendarioActividad::class.java)
        esperar {
            var tieneIndicadores = false
            calendario.onActivity { actividad ->
                val lista = actividad.findViewById<RecyclerView>(R.id.listaDiasCalendario)
                tieneIndicadores = (0 until lista.childCount).any { posicion ->
                    lista.getChildAt(posicion)
                        .findViewById<TextView>(R.id.indicadorActividad)
                        .visibility == View.VISIBLE
                }
            }
            tieneIndicadores
        }
        calendario.close()

        val resumen = ActivityScenario.launch(ResumenSemanalActividad::class.java)
        esperar { textosVista(resumen, R.id.pantallaResumenSemanal).contains("Rutina") }
        val textosResumen = textosVista(resumen, R.id.pantallaResumenSemanal)
        assertTrue(textosResumen.contains("completadas"))
        assertTrue(textosResumen.contains("eventos"))
        resumen.onActivity { actividad ->
            val rangoActual = actividad.findViewById<TextView>(R.id.textoRangoSemana).text.toString()
            actividad.findViewById<Button>(R.id.btnSemanaSiguiente).performClick()
            assertFalse(
                rangoActual == actividad.findViewById<TextView>(R.id.textoRangoSemana).text.toString()
            )
            actividad.findViewById<Button>(R.id.btnSemanaActual).performClick()
            assertEquals(rangoActual, actividad.findViewById<TextView>(R.id.textoRangoSemana).text.toString())
        }
        resumen.close()
    }

    private fun insertar(
        titulo: String,
        fecha: LocalDate,
        tipo: TipoActividad = TipoActividad.TAREA,
        prioridad: Prioridad,
        recurrencia: Recurrencia = Recurrencia.SIN_REPETICION
    ): Long = baseDeDatos.actividadDao().insertar(
        Actividad(
            tipo = tipo,
            titulo = titulo,
            descripcion = "Descripción de $titulo",
            fecha = fecha,
            hora = null,
            prioridad = prioridad,
            categoriaId = categoriaId,
            estadoTarea = if (tipo == TipoActividad.TAREA) EstadoTarea.PENDIENTE else null,
            recordatorio = null,
            recurrencia = recurrencia
        )
    )

    private fun pulsarAccionPrimeraTarjeta(
        escenario: ActivityScenario<ListaActividadesActividad>,
        listaId: Int,
        botonId: Int
    ) {
        escenario.onActivity { actividad ->
            val lista = actividad.findViewById<RecyclerView>(listaId)
            assertTrue("La sección no mostró ninguna tarjeta", lista.childCount > 0)
            lista.getChildAt(0).findViewById<Button>(botonId).performClick()
        }
    }

    private fun botonHabilitado(
        escenario: ActivityScenario<out android.app.Activity>,
        id: Int
    ): Boolean {
        var habilitado = false
        escenario.onActivity { habilitado = it.findViewById<View>(id).isEnabled }
        return habilitado
    }

    private fun textoVista(
        escenario: ActivityScenario<out android.app.Activity>,
        id: Int
    ): String {
        var texto = ""
        escenario.onActivity { texto = it.findViewById<TextView>(id).text.toString() }
        return texto
    }

    private fun visibilidadVista(
        escenario: ActivityScenario<out android.app.Activity>,
        id: Int
    ): Int {
        var visibilidad = View.GONE
        escenario.onActivity { visibilidad = it.findViewById<View>(id).visibility }
        return visibilidad
    }

    private fun textosVista(
        escenario: ActivityScenario<out android.app.Activity>,
        id: Int
    ): String {
        var textos = ""
        escenario.onActivity {
            textos = recolectarTextos(it.findViewById(id)).joinToString("\n")
        }
        return textos
    }

    private fun recolectarTextos(vista: View): List<String> = when (vista) {
        is TextView -> listOf(vista.text.toString())
        is ViewGroup -> (0 until vista.childCount).flatMap { recolectarTextos(vista.getChildAt(it)) }
        else -> emptyList()
    }

    private fun esperar(
        tiempoMaximoMs: Long = 5_000,
        condicion: () -> Boolean
    ) {
        val limite = System.currentTimeMillis() + tiempoMaximoMs
        var ultimoError: Throwable? = null
        while (System.currentTimeMillis() < limite) {
            try {
                if (condicion()) return
            } catch (error: Throwable) {
                ultimoError = error
            }
            Thread.sleep(50)
        }
        ultimoError?.let { throw it }
        assertTrue("La condición no se cumplió dentro del tiempo esperado", condicion())
    }

    private companion object {
        val FORMATO_FECHA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu")
    }
}
