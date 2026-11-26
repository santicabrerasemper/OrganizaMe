package com.santi.organizame.data.respaldo

import com.santi.organizame.domain.repository.RepositorioRespaldo
import com.santi.organizame.modelo.OrganizaMeDatabase
import com.santi.organizame.respaldo.DatosRespaldo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoomRespaldoRepository(
    private val baseDatos: OrganizaMeDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : RepositorioRespaldo {
    override suspend fun obtener(): DatosRespaldo = withContext(dispatcher) {
        DatosRespaldo(
            categorias = baseDatos.categoriaDao().obtenerTodas(),
            actividades = baseDatos.actividadDao().obtenerTodas()
        )
    }

    override suspend fun reemplazar(datos: DatosRespaldo) {
        withContext(dispatcher) {
            baseDatos.runInTransaction {
                baseDatos.actividadDao().eliminarTodas()
                baseDatos.categoriaDao().eliminarTodas()
                baseDatos.categoriaDao().insertarTodas(datos.categorias)
                baseDatos.actividadDao().insertarTodas(datos.actividades)
            }
        }
    }
}
