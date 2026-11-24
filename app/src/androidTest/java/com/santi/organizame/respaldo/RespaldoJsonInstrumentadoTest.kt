package com.santi.organizame.respaldo

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.Categoria
import com.santi.organizame.modelo.EstadoTarea
import com.santi.organizame.modelo.Prioridad
import com.santi.organizame.modelo.Recurrencia
import com.santi.organizame.modelo.TipoActividad
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class RespaldoJsonInstrumentadoTest {

    @Test
    fun conservaCategoriasActividadesYRecurrencia() {
        val datos = DatosRespaldo(
            categorias = listOf(
                Categoria(7, "Facultad", "#2563EB", listOf("parcial", "examen"))
            ),
            actividades = listOf(
                Actividad(
                    id = 12,
                    tipo = TipoActividad.TAREA,
                    titulo = "Preparar final",
                    descripcion = "Unidad cinco",
                    fecha = LocalDate.of(2026, 12, 1),
                    prioridad = Prioridad.ALTA,
                    categoriaId = 7,
                    estadoTarea = EstadoTarea.PENDIENTE,
                    recurrencia = Recurrencia.SEMANAL,
                    fechaCreacion = Instant.parse("2026-11-23T12:00:00Z"),
                    fechaModificacion = Instant.parse("2026-11-23T12:00:00Z")
                )
            )
        )

        assertEquals(datos, RespaldoJson.deserializar(RespaldoJson.serializar(datos)))
    }
}
