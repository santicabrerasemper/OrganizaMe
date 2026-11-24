package com.santi.organizame.respaldo

import com.santi.organizame.modelo.OrganizaMeDatabase
import java.util.concurrent.Executors

class RespaldoRepository(private val baseDatos: OrganizaMeDatabase) {

    fun obtener(alCompletar: (Result<DatosRespaldo>) -> Unit) {
        ejecutor.execute {
            alCompletar(runCatching {
                DatosRespaldo(
                    categorias = baseDatos.categoriaDao().obtenerTodas(),
                    actividades = baseDatos.actividadDao().obtenerTodas()
                )
            })
        }
    }

    fun reemplazar(
        datos: DatosRespaldo,
        alCompletar: (Result<Unit>) -> Unit
    ) {
        ejecutor.execute {
            alCompletar(runCatching {
                baseDatos.runInTransaction {
                    baseDatos.actividadDao().eliminarTodas()
                    baseDatos.categoriaDao().eliminarTodas()
                    baseDatos.categoriaDao().insertarTodas(datos.categorias)
                    baseDatos.actividadDao().insertarTodas(datos.actividades)
                }
            })
        }
    }

    private companion object {
        val ejecutor = Executors.newSingleThreadExecutor()
    }
}
