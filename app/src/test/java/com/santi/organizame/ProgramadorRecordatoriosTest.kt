package com.santi.organizame

import com.santi.organizame.modelo.AnticipacionRecordatorio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class ProgramadorRecordatoriosTest {

    private val zona = ZoneId.of("America/Argentina/Buenos_Aires")
    private val fecha = LocalDate.of(2026, 11, 20)
    private val hora = LocalTime.of(18, 0)

    @Test
    fun programaAvisoALaHoraDeLaActividad() {
        val momento = ProgramadorRecordatorios.calcularMomentoAviso(
            fecha,
            hora,
            AnticipacionRecordatorio.A_LA_HORA,
            zona
        )

        assertEquals(Instant.parse("2026-11-20T21:00:00Z"), momento)
    }

    @Test
    fun descuentaLaAnticipacionSeleccionada() {
        val momento = ProgramadorRecordatorios.calcularMomentoAviso(
            fecha,
            hora,
            AnticipacionRecordatorio.TREINTA_MINUTOS_ANTES,
            zona
        )

        assertEquals(Instant.parse("2026-11-20T20:30:00Z"), momento)
    }

    @Test
    fun admiteRecordatorioDelDiaAnterior() {
        val momento = ProgramadorRecordatorios.calcularMomentoAviso(
            fecha,
            hora,
            AnticipacionRecordatorio.UN_DIA_ANTES,
            zona
        )

        assertEquals(Instant.parse("2026-11-19T21:00:00Z"), momento)
    }

    @Test
    fun noCalculaAvisoSiFaltanDatos() {
        assertNull(
            ProgramadorRecordatorios.calcularMomentoAviso(
                fecha,
                null,
                AnticipacionRecordatorio.DIEZ_MINUTOS_ANTES,
                zona
            )
        )
        assertNull(
            ProgramadorRecordatorios.calcularMomentoAviso(
                fecha,
                hora,
                null,
                zona
            )
        )
    }

    @Test
    fun usaAlarmaExactaSinPermisoEspecialAntesDeAndroid12() {
        assertEquals(
            true,
            ProgramadorRecordatorios.debeUsarAlarmaExacta(
                sdkInt = 30,
                permisoConcedido = false
            )
        )
    }

    @Test
    fun usaAlarmaExactaConPermisoEnAndroid12OSuperior() {
        assertEquals(
            true,
            ProgramadorRecordatorios.debeUsarAlarmaExacta(
                sdkInt = 31,
                permisoConcedido = true
            )
        )
    }

    @Test
    fun usaAlternativaAproximadaSinPermisoEnAndroid12OSuperior() {
        assertEquals(
            false,
            ProgramadorRecordatorios.debeUsarAlarmaExacta(
                sdkInt = 31,
                permisoConcedido = false
            )
        )
    }
}
