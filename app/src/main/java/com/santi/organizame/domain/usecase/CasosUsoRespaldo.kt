package com.santi.organizame.domain.usecase

import com.santi.organizame.domain.recordatorios.GestorRecordatorios
import com.santi.organizame.domain.repository.RepositorioRespaldo
import com.santi.organizame.respaldo.DatosRespaldo
import com.santi.organizame.respaldo.RespaldoJson
import java.time.Instant

data class RespaldoPreparado(
    val texto: String,
    val actividades: Int,
    val categorias: Int
)

class PrepararRespaldoUseCase(
    private val repositorio: RepositorioRespaldo
) {
    suspend operator fun invoke(): RespaldoPreparado {
        val datos = repositorio.obtener()
        return RespaldoPreparado(
            texto = RespaldoJson.serializar(datos),
            actividades = datos.actividades.size,
            categorias = datos.categorias.size
        )
    }
}

class AnalizarRespaldoUseCase {
    operator fun invoke(texto: String): DatosRespaldo =
        RespaldoJson.deserializar(texto)
}

class ImportarRespaldoUseCase(
    private val repositorio: RepositorioRespaldo,
    private val recordatorios: GestorRecordatorios
) {
    suspend operator fun invoke(datos: DatosRespaldo) {
        val actuales = repositorio.obtener()
        val ahora = Instant.now()
        val normalizados = datos.copy(
            actividades = datos.actividades.map { actividad ->
                actividad.copy(
                    fechaRecordatorioProgramado =
                        com.santi.organizame.domain.recordatorios.CalculadorMomentoRecordatorio
                            .calcular(
                                actividad.fecha,
                                actividad.hora,
                                actividad.recordatorio
                            )
                            ?.takeIf { it.isAfter(ahora) }
                )
            }
        )

        repositorio.reemplazar(normalizados)
        actuales.actividades.forEach { runCatching { recordatorios.cancelar(it.id) } }
        normalizados.actividades.forEach { runCatching { recordatorios.programar(it) } }
    }
}
