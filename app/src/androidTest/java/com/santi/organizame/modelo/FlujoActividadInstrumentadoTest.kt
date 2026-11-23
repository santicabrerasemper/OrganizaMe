package com.santi.organizame.modelo

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class FlujoActividadInstrumentadoTest {

    private lateinit var baseDeDatos: OrganizaMeDatabase
    private lateinit var actividades: ActividadRepository
    private lateinit var categorias: CategoriaRepository

    @Before
    fun prepararFlujo() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        baseDeDatos = Room.inMemoryDatabaseBuilder(
            contexto,
            OrganizaMeDatabase::class.java
        ).build()
        actividades = ActividadRepository(baseDeDatos.actividadDao())
        categorias = CategoriaRepository(baseDeDatos.categoriaDao())
    }

    @After
    fun cerrarBaseDeDatos() {
        baseDeDatos.close()
    }

    @Test
    fun creaConsultaCompletaListaYEliminaUnaActividad() {
        val categoriaId = esperar { callback ->
            categorias.guardar(
                Categoria(
                    nombre = "Facultad",
                    color = "#3F51B5",
                    palabrasClave = listOf("parcial")
                ),
                callback
            )
        }
        val nuevaActividad = Actividad(
            tipo = TipoActividad.TAREA,
            titulo = "Preparar parcial",
            descripcion = "Resolver ejercicios",
            fecha = LocalDate.of(2026, 11, 30),
            hora = LocalTime.of(19, 0),
            prioridad = Prioridad.ALTA,
            categoriaId = categoriaId,
            estadoTarea = EstadoTarea.PENDIENTE,
            recordatorio = AnticipacionRecordatorio.UNA_HORA_ANTES,
            fechaCreacion = Instant.parse("2026-11-23T15:00:00Z"),
            fechaModificacion = Instant.parse("2026-11-23T15:00:00Z")
        )

        val actividadId = esperar { callback ->
            actividades.guardar(nuevaActividad, callback)
        }
        val guardada: Actividad = checkNotNull(
            esperar<Actividad?> { callback ->
                actividades.obtenerPorId(actividadId, callback)
            }
        )
        assertEquals(nuevaActividad.copy(id = actividadId), guardada)

        val completada = guardada.copy(
            estadoTarea = EstadoTarea.COMPLETADA,
            fechaCompletada = Instant.parse("2026-11-23T18:00:00Z"),
            fechaModificacion = Instant.parse("2026-11-23T18:00:00Z")
        )
        esperar { callback -> actividades.actualizar(completada, callback) }

        val listado = esperar { callback -> actividades.obtenerTodas(callback) }
        assertEquals(listOf(actividadId), listado.map(Actividad::id))
        assertEquals(EstadoTarea.COMPLETADA, listado.single().estadoTarea)

        esperar { callback -> actividades.eliminar(listado.single(), callback) }
        assertNull(esperar { callback -> actividades.obtenerPorId(actividadId, callback) })
    }

    private fun <T> esperar(
        operacion: (((Result<T>) -> Unit) -> Unit)
    ): T {
        val cerrojo = CountDownLatch(1)
        val resultado = AtomicReference<Result<T>>()

        operacion { valor ->
            resultado.set(valor)
            cerrojo.countDown()
        }

        assertTrue(
            "La operación del repositorio no finalizó dentro del tiempo esperado",
            cerrojo.await(5, TimeUnit.SECONDS)
        )
        return checkNotNull(resultado.get()).getOrThrow()
    }
}
