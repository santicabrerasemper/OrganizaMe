package com.santi.organizame.domain.repository

import com.santi.organizame.respaldo.DatosRespaldo

interface RepositorioRespaldo {
    suspend fun obtener(): DatosRespaldo
    suspend fun reemplazar(datos: DatosRespaldo)
}
