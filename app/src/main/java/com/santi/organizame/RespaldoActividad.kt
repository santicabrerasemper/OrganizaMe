package com.santi.organizame

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.santi.organizame.domain.usecase.RespaldoPreparado
import com.santi.organizame.respaldo.DatosRespaldo
import com.santi.organizame.ui.respaldo.RespaldoEfecto
import com.santi.organizame.ui.respaldo.RespaldoViewModel
import com.santi.organizame.ui.respaldo.RespaldoViewModelFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RespaldoActividad : AppCompatActivity() {

    private val viewModel: RespaldoViewModel by viewModels {
        RespaldoViewModelFactory(applicationContext)
    }
    private var destinoExportacion: Uri? = null

    private val crearDocumento = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            destinoExportacion = it
            mostrarEstado("Preparando respaldo…")
            viewModel.prepararExportacion()
        }
    }

    private val abrirDocumento = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(::leerRespaldo) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_respaldo)
        aplicarInsets()

        findViewById<Button>(R.id.btnVolverRespaldo).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnExportarRespaldo).setOnClickListener {
            val marca = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")
            )
            crearDocumento.launch("organizame-$marca.json")
        }
        findViewById<Button>(R.id.btnImportarRespaldo).setOnClickListener {
            abrirDocumento.launch(arrayOf("application/json", "text/plain"))
        }
        observarViewModel()
    }

    private fun observarViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.estado.collect { estado ->
                        findViewById<Button>(R.id.btnExportarRespaldo).isEnabled =
                            !estado.procesando
                        findViewById<Button>(R.id.btnImportarRespaldo).isEnabled =
                            !estado.procesando
                    }
                }
                launch { viewModel.efectos.collect(::procesarEfecto) }
            }
        }
    }

    private fun procesarEfecto(efecto: RespaldoEfecto) {
        when (efecto) {
            is RespaldoEfecto.Exportar -> escribirRespaldo(efecto.respaldo)
            is RespaldoEfecto.ConfirmarImportacion -> confirmarImportacion(efecto.datos)
            is RespaldoEfecto.Mensaje -> {
                mostrarEstado(efecto.texto)
                if (efecto.error || efecto.toastCorto) {
                    Toast.makeText(
                        this,
                        if (efecto.toastCorto) "Datos recuperados" else efecto.texto,
                        if (efecto.toastCorto) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun escribirRespaldo(respaldo: RespaldoPreparado) {
        val uri = destinoExportacion ?: run {
            mostrarError("No se pudo abrir el archivo")
            return
        }
        lifecycleScope.launch {
            val resultado = withContext(Dispatchers.IO) {
                runCatching {
                    contentResolver.openOutputStream(uri)?.bufferedWriter()?.use {
                        it.write(respaldo.texto)
                    } ?: error("No se pudo abrir el archivo")
                }
            }
            resultado.onSuccess {
                mostrarEstado(
                    "Respaldo exportado: ${respaldo.actividades} actividades y " +
                        "${respaldo.categorias} categorías."
                )
            }.onFailure {
                mostrarError("No se pudo exportar el respaldo")
            }
            destinoExportacion = null
        }
    }

    private fun leerRespaldo(uri: Uri) {
        mostrarEstado("Revisando archivo…")
        lifecycleScope.launch {
            val resultado = withContext(Dispatchers.IO) {
                runCatching {
                    contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                        it.readText()
                    } ?: error("No se pudo abrir el archivo")
                }
            }
            resultado.onSuccess(viewModel::analizar)
                .onFailure { mostrarError("El archivo no es un respaldo válido") }
        }
    }

    private fun confirmarImportacion(datos: DatosRespaldo) {
        AlertDialog.Builder(this)
            .setTitle("Importar respaldo")
            .setMessage(
                "Se reemplazarán los datos actuales por ${datos.actividades.size} actividades " +
                    "y ${datos.categorias.size} categorías. ¿Querés continuar?"
            )
            .setPositiveButton("Importar") { _, _ ->
                mostrarEstado("Importando respaldo…")
                viewModel.importar(datos)
            }
            .setNegativeButton("Cancelar") { _, _ ->
                mostrarEstado("Importación cancelada.")
            }
            .setOnCancelListener { mostrarEstado("Importación cancelada.") }
            .show()
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
}
