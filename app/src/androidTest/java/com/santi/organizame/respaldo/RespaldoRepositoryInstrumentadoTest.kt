package com.santi.organizame.respaldo

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class RespaldoRepositoryInstrumentadoTest {

    private lateinit var baseDatos: OrganizaMeDatabase
    private lateinit var repositorio: RespaldoRepository

    @Before
    fun preparar() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        baseDatos = Room.inMemoryDatabaseBuilder(
            contexto,
            OrganizaMeDatabase::class.java
        ).build()
        repositorio = RespaldoRepository(baseDatos)
    }

    @After
    fun cerrar() = baseDatos.close()

    @Test
    fun reemplazaCategoriasYActividadesEnUnaTransaccion() {
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

        esperar { callback -> repositorio.reemplazar(datos, callback) }
        val recuperados = esperar<DatosRespaldo> { callback ->
            repositorio.obtener(callback)
        }

        assertEquals(datos, recuperados)
    }

    private fun <T> esperar(operacion: (((Result<T>) -> Unit) -> Unit)): T {
        val cerrojo = CountDownLatch(1)
        val resultado = AtomicReference<Result<T>>()
        operacion {
            resultado.set(it)
            cerrojo.countDown()
        }
        assertTrue(cerrojo.await(5, TimeUnit.SECONDS))
        return checkNotNull(resultado.get()).getOrThrow()
    }
}
