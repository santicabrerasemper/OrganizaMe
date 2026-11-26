package com.santi.organizame.data.recordatorios

import com.santi.organizame.domain.recordatorios.GestorRecordatorios
import com.santi.organizame.domain.recordatorios.ResultadoProgramacionRecordatorio
import com.santi.organizame.modelo.Actividad

class GestorRecordatoriosAndroid(
    private val programador: ProgramadorRecordatorios
) : GestorRecordatorios {
    override fun programar(actividad: Actividad): ResultadoProgramacionRecordatorio =
        when (programador.programar(actividad)) {
            ProgramadorRecordatorios.ResultadoProgramacion.EXACTO ->
                ResultadoProgramacionRecordatorio.EXACTO
            ProgramadorRecordatorios.ResultadoProgramacion.APROXIMADO ->
                ResultadoProgramacionRecordatorio.APROXIMADO
            ProgramadorRecordatorios.ResultadoProgramacion.NO_PROGRAMADO ->
                ResultadoProgramacionRecordatorio.NO_PROGRAMADO
        }

    override fun cancelar(actividadId: Long) {
        programador.cancelar(actividadId)
    }
}
