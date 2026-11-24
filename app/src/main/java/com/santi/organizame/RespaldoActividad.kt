package com.santi.organizame

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.santi.organizame.modelo.OrganizaMeDatabase
import com.santi.organizame.respaldo.DatosRespaldo
import com.santi.organizame.respaldo.RespaldoJson
import com.santi.organizame.respaldo.RespaldoRepository
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors

class RespaldoActividad : AppCompatActivity() {

    private lateinit var repositorio: RespaldoRepository

    private val crearDocumento = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let(::exportarA) }

    private val abrirDocumento = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(::leerRespaldo) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_respaldo)
        aplicarInsets()

        repositorio = RespaldoRepository(OrganizaMeDatabase.obtener(applicationContext))

        findViewById<Button>(R.id.btnVolverRespaldo).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnExportarRespaldo).setOnClickListener {
            val marca = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))
            crearDocumento.launch("organizame-$marca.json")
        }
        findViewById<Button>(R.id.btnImportarRespaldo).setOnClickListener {
            abrirDocumento.launch(arrayOf("application/json", "text/plain"))
        }
    }

    private fun exportarA(uri: Uri) {
        mostrarEstado("Preparando respaldo…")
        repositorio.obtener { resultado ->
            resultado.onSuccess { datos ->
                val escritura = runCatching {
                    contentResolver.openOutputStream(uri)?.bufferedWriter()?.use {
                        it.write(RespaldoJson.serializar(datos))
                    } ?: error("No se pudo abrir el archivo")
                }
                runOnUiThread {
                    escritura.onSuccess {
                        mostrarEstado(
                            "Respaldo exportado: ${datos.actividades.size} actividades y " +
                                    "${datos.categorias.size} categorías."
                        )
                    }.onFailure { mostrarError("No se pudo exportar el respaldo") }
                }
            }.onFailure {
                runOnUiThread { mostrarError("No se pudieron leer los datos") }
            }
        }
    }

    private fun leerRespaldo(uri: Uri) {
        mostrarEstado("Revisando archivo…")
        ejecutor.execute {
            val resultado = runCatching {
                val texto = contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                    it.readText()
                } ?: error("No se pudo abrir el archivo")
                RespaldoJson.deserializar(texto)
            }
            runOnUiThread {
                resultado.onSuccess(::confirmarImportacion)
                    .onFailure { mostrarError("El archivo no es un respaldo válido") }
            }
        }
    }

    private fun confirmarImportacion(datos: DatosRespaldo) {
        AlertDialog.Builder(this)
            .setTitle("Importar respaldo")
            .setMessage(
                "Se reemplazarán los datos actuales por ${datos.actividades.size} actividades " +
                        "y ${datos.categorias.size} categorías. ¿Querés continuar?"
            )
            .setPositiveButton("Importar") { _, _ -> importar(datos) }
            .setNegativeButton("Cancelar") { _, _ ->
                mostrarEstado("Importación cancelada.")
            }
            .setOnCancelListener {
                mostrarEstado("Importación cancelada.")
            }
            .show()
    }

    private fun importar(datos: DatosRespaldo) {
        mostrarEstado("Importando respaldo…")
        repositorio.obtener { actuales ->
            actuales.onSuccess { datosActuales ->
                val ahora = Instant.now()
                val normalizados = datos.copy(
                    actividades = datos.actividades.map { actividad ->
                        actividad.copy(
                            fechaRecordatorioProgramado =
                                ProgramadorRecordatorios.calcularMomentoAviso(
                                    actividad.fecha,
                                    actividad.hora,
                                    actividad.recordatorio
                                )?.takeIf { it.isAfter(ahora) }
                        )
                    }
                )

                repositorio.reemplazar(normalizados) { resultado ->
                    runOnUiThread {
                        resultado.onSuccess {
                            val programador = ProgramadorRecordatorios(applicationContext)
                            datosActuales.actividades.forEach { programador.cancelar(it.id) }
                            normalizados.actividades.forEach(programador::programar)
                            mostrarEstado("Respaldo importado correctamente.")
                            Toast.makeText(this, "Datos recuperados", Toast.LENGTH_SHORT).show()
                        }.onFailure { mostrarError("No se pudo importar el respaldo") }
                    }
                }
            }.onFailure {
                runOnUiThread { mostrarError("No se pudieron preparar los datos actuales") }
            }
        }
    }

    private fun mostrarEstado(mensaje: String) {
        findViewById<TextView>(R.id.textoEstadoRespaldo).text = mensaje
    }

    private fun mostrarError(mensaje: String) {
        mostrarEstado(mensaje)
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }

    private fun aplicarInsets() {
        val vista = findViewById<View>(R.id.pantallaRespaldo)
        val izquierda = vista.paddingLeft
        val arriba = vista.paddingTop
        val derecha = vista.paddingRight
        val abajo = vista.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(vista) { _, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            vista.setPadding(
                izquierda + barras.left,
                arriba + barras.top,
                derecha + barras.right,
                abajo + barras.bottom
            )
            insets
        }
        ViewCompat.requestApplyInsets(vista)
    }

    private companion object {
        val ejecutor = Executors.newSingleThreadExecutor()
    }
}
