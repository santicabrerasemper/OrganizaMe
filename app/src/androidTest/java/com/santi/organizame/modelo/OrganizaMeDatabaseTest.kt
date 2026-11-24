package com.santi.organizame.modelo

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

@RunWith(AndroidJUnit4::class)
class OrganizaMeDatabaseTest {

    private lateinit var baseDeDatos: OrganizaMeDatabase
    private lateinit var actividadDao: ActividadDao
    private lateinit var categoriaDao: CategoriaDao

    @Before
    fun crearBaseDeDatos() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        baseDeDatos = Room.inMemoryDatabaseBuilder(
            contexto,
            OrganizaMeDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        actividadDao = baseDeDatos.actividadDao()
        categoriaDao = baseDeDatos.categoriaDao()
    }

    @After
    fun cerrarBaseDeDatos() {
        baseDeDatos.close()
    }

    @Test
    fun persisteYRecuperaTodosLosCamposDeUnaActividad() {
        val categoriaId = categoriaDao.insertar(
            Categoria(
                nombre = "Estudio",
                color = "#2196F3",
                palabrasClave = listOf("facultad", "universidad")
            )
        )
        val fechaCreacion = Instant.parse("2026-11-23T12:00:00Z")
        val fechaRecordatorio = Instant.parse("2026-11-24T20:30:00Z")
        val actividad = Actividad(
            tipo = TipoActividad.TAREA,
            titulo = "Preparar final",
            descripcion = "Repasar la unidad cinco",
            fecha = LocalDate.of(2026, 11, 25),
            hora = LocalTime.of(18, 0),
            prioridad = Prioridad.ALTA,
            categoriaId = categoriaId,
            estadoTarea = EstadoTarea.PENDIENTE,
            recordatorio = AnticipacionRecordatorio.TREINTA_MINUTOS_ANTES,
            fechaRecordatorioProgramado = fechaRecordatorio,
            recurrencia = Recurrencia.SEMANAL,
            fechaCreacion = fechaCreacion,
            fechaModificacion = fechaCreacion
        )

        val actividadId = actividadDao.insertar(actividad)
        val guardada = actividadDao.obtenerPorId(actividadId)

        assertNotNull(guardada)
        assertEquals(actividad.copy(id = actividadId), guardada)
    }

    @Test
    fun eliminarCategoriaConservaLaActividadSinCategoria() {
        val categoriaId = categoriaDao.insertar(
            Categoria(nombre = "Trabajo", color = "#FF9800")
        )
        val actividadId = actividadDao.insertar(
            Actividad(
                tipo = TipoActividad.EVENTO,
                titulo = "Reunión",
                categoriaId = categoriaId
            )
        )

        categoriaDao.eliminar(checkNotNull(categoriaDao.obtenerPorId(categoriaId)))

        assertNull(categoriaDao.obtenerPorId(categoriaId))
        assertNull(checkNotNull(actividadDao.obtenerPorId(actividadId)).categoriaId)
    }
}
