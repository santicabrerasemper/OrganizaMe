package com.santi.organizame.modelo

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.santi.organizame.data.repository.RoomActividadRepository
import com.santi.organizame.data.repository.RoomCategoriaRepository
import com.santi.organizame.domain.repository.RepositorioActividades
import com.santi.organizame.domain.repository.RepositorioCategorias
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

@RunWith(AndroidJUnit4::class)
class FlujoActividadInstrumentadoTest {

    private lateinit var baseDeDatos: OrganizaMeDatabase
    private lateinit var actividades: RepositorioActividades
    private lateinit var categorias: RepositorioCategorias

    @Before
    fun prepararFlujo() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        baseDeDatos = Room.inMemoryDatabaseBuilder(
            contexto,
            OrganizaMeDatabase::class.java
        ).build()
        actividades = RoomActividadRepository(baseDeDatos.actividadDao())
        categorias = RoomCategoriaRepository(baseDeDatos.categoriaDao())
    }

    @After
    fun cerrarBaseDeDatos() {
        baseDeDatos.close()
    }

    @Test
    fun creaConsultaCompletaListaYEliminaUnaActividad() = runBlocking {
        val categoriaId = categorias.guardar(
            Categoria(
                nombre = "Facultad",
                color = "#3F51B5",
                palabrasClave = listOf("parcial")
            )
        )
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

        val actividadId = actividades.guardar(nuevaActividad)
        val guardada = checkNotNull(actividades.obtenerPorId(actividadId))
        assertEquals(nuevaActividad.copy(id = actividadId), guardada)

        val completada = guardada.copy(
            estadoTarea = EstadoTarea.COMPLETADA,
            fechaCompletada = Instant.parse("2026-11-23T18:00:00Z"),
            fechaModificacion = Instant.parse("2026-11-23T18:00:00Z")
        )
        actividades.actualizar(completada)

        val listado = actividades.obtenerTodas()
        assertEquals(listOf(actividadId), listado.map(Actividad::id))
        assertEquals(EstadoTarea.COMPLETADA, listado.single().estadoTarea)

        actividades.eliminar(listado.single())
        assertNull(actividades.obtenerPorId(actividadId))
    }
}
