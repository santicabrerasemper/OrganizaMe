package com.santi.organizame

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatDelegate
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.recyclerview.widget.RecyclerView
import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.EstadoTarea
import com.santi.organizame.modelo.OrganizaMeDatabase
import com.santi.organizame.modelo.TipoActividad
import com.santi.organizame.data.recordatorios.ProgramadorRecordatorios
import com.santi.organizame.ui.categorias.CategoriasActividad
import com.santi.organizame.ui.formulario.NuevaTareaActividad
import com.santi.organizame.ui.inicio.MainActivity
import com.santi.organizame.ui.lista.ListaActividadesActividad
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class FlujosAdicionalesUiInstrumentadoTest {

    private lateinit var contexto: Context
    private lateinit var baseDeDatos: OrganizaMeDatabase

    @Before
    fun preparar() {
        contexto = ApplicationProvider.getApplicationContext()
        baseDeDatos = OrganizaMeDatabase.obtener(contexto)
        baseDeDatos.clearAllTables()
        contexto.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @After
    fun limpiar() {
        baseDeDatos.actividadDao().obtenerTodas().forEach {
            ProgramadorRecordatorios(contexto).cancelar(it.id)
        }
        baseDeDatos.clearAllTables()
        contexto.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    @Test
    fun categoriasCreaEditaYEliminaDesdeLaInterfaz() {
        val escenario = ActivityScenario.launch(CategoriasActividad::class.java)

        onView(withId(R.id.btnNuevaCategoria)).perform(click())
        onView(withHint("Nombre de la categoría"))
            .perform(replaceText("Salud"), closeSoftKeyboard())
        onView(withHint("Palabras clave separadas por comas"))
            .perform(replaceText("médico, turno"), closeSoftKeyboard())
        onView(allOf(withText("Guardar"), isDisplayed())).perform(click())

        esperar {
            baseDeDatos.categoriaDao().obtenerTodas().singleOrNull()?.nombre == "Salud"
        }
        assertEquals(
            listOf("médico", "turno"),
            baseDeDatos.categoriaDao().obtenerTodas().single().palabrasClave
        )

        onView(withId(R.id.btnEditarCategoria)).perform(click())
        onView(
            allOf(
                isAssignableFrom(EditText::class.java),
                withText("Salud"),
                isDisplayed()
            )
        )
            .perform(replaceText("Bienestar"))
        onView(withHint("Palabras clave separadas por comas"))
            .perform(replaceText("salud, control"), closeSoftKeyboard())
        onView(allOf(withText("Guardar"), isDisplayed())).perform(click())

        esperar {
            baseDeDatos.categoriaDao().obtenerTodas().singleOrNull()?.nombre == "Bienestar"
        }
        assertEquals(
            listOf("salud", "control"),
            baseDeDatos.categoriaDao().obtenerTodas().single().palabrasClave
        )

        onView(withId(R.id.btnEliminarCategoria)).perform(click())
        onView(allOf(withText("Eliminar"), isDisplayed())).perform(click())
        esperar { baseDeDatos.categoriaDao().obtenerTodas().isEmpty() }

        escenario.close()
    }

    @Test
    fun modoOscuroSeGuardaYSeRecuperaAlReabrir() {
        val preferencias = contexto.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
        val escenario = ActivityScenario.launch(MainActivity::class.java)

        escenario.onActivity {
            assertEquals(
                "Activar modo oscuro",
                it.findViewById<Button>(R.id.btnModoOscuro).text.toString()
            )
            it.findViewById<Button>(R.id.btnModoOscuro).performClick()
        }
        esperar { preferencias.getBoolean("modo_oscuro", false) }
        escenario.close()

        val reapertura = ActivityScenario.launch(MainActivity::class.java)
        esperar {
            textoBoton(reapertura, R.id.btnModoOscuro) == "Desactivar modo oscuro"
        }
        assertTrue(preferencias.getBoolean("modo_oscuro", false))
        reapertura.close()
    }

    @Test
    fun formularioValidaCamposYListadoEliminaLaActividad() {
        val formulario = ActivityScenario.launch<NuevaTareaActividad>(
            Intent(contexto, NuevaTareaActividad::class.java)
        )
        esperar { botonHabilitado(formulario, R.id.btnGuardarTarea) }

        formulario.onActivity { actividad ->
            actividad.findViewById<Button>(R.id.btnGuardarTarea).performClick()
            assertNotNull(actividad.findViewById<EditText>(R.id.tituloTarea).error)

            actividad.findViewById<EditText>(R.id.tituloTarea).setText("Evento sin fecha")
            actividad.findViewById<Spinner>(R.id.tipoActividad).setSelection(1)
            actividad.findViewById<Button>(R.id.btnGuardarTarea).performClick()
            assertNotNull(actividad.findViewById<EditText>(R.id.fechaTarea).error)
        }
        formulario.close()

        val id = baseDeDatos.actividadDao().insertar(
            Actividad(
                tipo = TipoActividad.TAREA,
                titulo = "Actividad eliminable",
                fecha = LocalDate.now().plusDays(1),
                estadoTarea = EstadoTarea.PENDIENTE
            )
        )
        val listado = ActivityScenario.launch(ListaActividadesActividad::class.java)
        esperar { textoBoton(listado, R.id.btnPendientes).contains("(1)") }
        listado.onActivity {
            it.findViewById<Button>(R.id.btnPendientes).performClick()
        }
        esperar { vistaVisible(listado, R.id.listaPendientes) }

        listado.onActivity { actividad ->
            val lista = actividad.findViewById<RecyclerView>(R.id.listaPendientes)
            assertTrue(lista.childCount > 0)
            lista.getChildAt(0)
                .findViewById<Button>(R.id.btnEliminarActividad)
                .performClick()
        }
        onView(allOf(withText("Eliminar"), isDisplayed())).perform(click())
        esperar { baseDeDatos.actividadDao().obtenerPorId(id) == null }

        listado.close()
    }

    private fun botonHabilitado(
        escenario: ActivityScenario<out android.app.Activity>,
        id: Int
    ): Boolean {
        var habilitado = false
        escenario.onActivity { habilitado = it.findViewById<View>(id).isEnabled }
        return habilitado
    }

    private fun vistaVisible(
        escenario: ActivityScenario<out android.app.Activity>,
        id: Int
    ): Boolean {
        var visible = false
        escenario.onActivity { visible = it.findViewById<View>(id).visibility == View.VISIBLE }
        return visible
    }

    private fun textoBoton(
        escenario: ActivityScenario<out android.app.Activity>,
        id: Int
    ): String {
        var texto = ""
        escenario.onActivity { texto = it.findViewById<Button>(id).text.toString() }
        return texto
    }

    private fun esperar(
        tiempoMaximoMs: Long = 7_000,
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
        const val PREFERENCIAS = "ajustes_organizame"
    }
}
