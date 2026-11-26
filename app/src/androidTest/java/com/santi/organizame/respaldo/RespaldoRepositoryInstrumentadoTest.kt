package com.santi.organizame.respaldo

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.santi.organizame.data.respaldo.RoomRespaldoRepository
import com.santi.organizame.domain.recordatorios.GestorRecordatorios
import com.santi.organizame.domain.recordatorios.ResultadoProgramacionRecordatorio
import com.santi.organizame.domain.repository.RepositorioRespaldo
import com.santi.organizame.domain.usecase.AnalizarRespaldoUseCase
import com.santi.organizame.domain.usecase.ImportarRespaldoUseCase
import com.santi.organizame.domain.usecase.PrepararRespaldoUseCase
import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.Categoria
import com.santi.organizame.modelo.OrganizaMeDatabase
import com.santi.organizame.modelo.TipoActividad
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.Instant
import kotlinx.coroutines.runBlocking

@RunWith(AndroidJUnit4::class)
class RespaldoRepositoryInstrumentadoTest {

    private lateinit var baseDatos: OrganizaMeDatabase
    private lateinit var repositorio: RepositorioRespaldo

    @Before
    fun preparar() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        baseDatos = Room.inMemoryDatabaseBuilder(
            contexto,
            OrganizaMeDatabase::class.java
        ).build()
        repositorio = RoomRespaldoRepository(baseDatos)
    }

    @After
    fun cerrar() = baseDatos.close()

    @Test
    fun reemplazaCategoriasYActividadesEnUnaTransaccion() = runBlocking {
        val momento = Instant.parse("2026-11-23T12:00:00Z")
        val datos = DatosRespaldo(
            categorias = listOf(Categoria(20, "Nueva", "#0F766E")),
            actividades = listOf(
                Actividad(
                    id = 30,
                    tipo = TipoActividad.EVENTO,
                    titulo = "Evento importado",
                    fecha = LocalDate.of(2026, 12, 10),
                    categoriaId = 20,
                    fechaCreacion = momento,
                    fechaModificacion = momento
                )
            )
        )

        repositorio.reemplazar(datos)
        val recuperados = repositorio.obtener()

        assertEquals(datos, recuperados)
    }

    @Test
    fun exportaAnalizaImportaYReemplazaElContenidoCompleto() = runBlocking {
        val momento = Instant.parse("2026-11-23T12:00:00Z")
        val originales = DatosRespaldo(
            categorias = listOf(Categoria(7, "Facultad", "#2563EB")),
            actividades = listOf(
                Actividad(
                    id = 12,
                    tipo = TipoActividad.EVENTO,
                    titulo = "Defensa final",
                    fecha = LocalDate.of(2026, 12, 10),
                    categoriaId = 7,
                    fechaCreacion = momento,
                    fechaModificacion = momento
                )
            )
        )
        repositorio.reemplazar(originales)

        val exportado = PrepararRespaldoUseCase(repositorio)()
        assertEquals(1, exportado.actividades)
        assertEquals(1, exportado.categorias)
        val analizado = AnalizarRespaldoUseCase()(exportado.texto)
        assertEquals(originales, analizado)

        repositorio.reemplazar(
            DatosRespaldo(
                categorias = emptyList(),
                actividades = listOf(
                    Actividad(
                        id = 99,
                        tipo = TipoActividad.TAREA,
                        titulo = "Dato que será reemplazado",
                        fechaCreacion = momento,
                        fechaModificacion = momento
                    )
                )
            )
        )
        val gestor = GestorRecordatoriosFalso()
        ImportarRespaldoUseCase(repositorio, gestor)(analizado)

        assertEquals(originales, repositorio.obtener())
        assertEquals(listOf(99L), gestor.cancelados)
        assertEquals(listOf(12L), gestor.programados)
        assertTrue(exportado.texto.contains("Defensa final"))
    }
}

private class GestorRecordatoriosFalso : GestorRecordatorios {
    val programados = mutableListOf<Long>()
    val cancelados = mutableListOf<Long>()

    override fun programar(actividad: Actividad): ResultadoProgramacionRecordatorio {
        programados += actividad.id
        return ResultadoProgramacionRecordatorio.EXACTO
    }

    override fun cancelar(actividadId: Long) {
        cancelados += actividadId
    }
}
