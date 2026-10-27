package com.santi.organizame

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.santi.organizame.modelo.Categoria
import android.widget.LinearLayout
import com.santi.organizame.modelo.CategoriaRepository
import com.santi.organizame.modelo.OrganizaMeDatabase

class CategoriasActividad : AppCompatActivity() {

    private lateinit var categoriaRepository: CategoriaRepository
    private lateinit var adaptador: AdaptadorTarjetasCategoria
    private lateinit var textoEstado: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.actividad_categorias)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Botón Volver
        findViewById<Button>(R.id.btnVolverCategorias).setOnClickListener {
            finish()
        }

        textoEstado = findViewById(R.id.textoEstadoCategorias)

        categoriaRepository = CategoriaRepository(
            OrganizaMeDatabase.obtener(applicationContext).categoriaDao()
        )

        // Adaptador de las tarjetas
        adaptador = AdaptadorTarjetasCategoria(
            alEditar = { categoria ->
                editarCategoria(categoria)
            },
            alEliminar = { categoria ->
                eliminarCategoria(categoria)
            }
        )

        val listaCategorias = findViewById<RecyclerView>(R.id.listaCategorias)
        listaCategorias.layoutManager = LinearLayoutManager(this)
        listaCategorias.adapter = adaptador

        // Botón Nueva categoría
        findViewById<Button>(R.id.btnNuevaCategoria).setOnClickListener {
            crearCategoria()
        }

        cargarCategorias()
    }

    // Consultar las categorías guardadas
    private fun cargarCategorias() {
        categoriaRepository.obtenerTodas { resultado ->
            runOnUiThread {
                resultado
                    .onSuccess { categorias ->
                        adaptador.actualizarLista(categorias)

                        if (categorias.isEmpty()) {
                            textoEstado.visibility = View.VISIBLE
                            textoEstado.text = "No hay categorías guardadas"
                        } else {
                            textoEstado.visibility = View.GONE
                        }
                    }
                    .onFailure {
                        textoEstado.visibility = View.VISIBLE
                        textoEstado.text = "Error al cargar las categorías"
                    }
            }
        }
    }

    // Crear una categoría nueva
    private fun crearCategoria() {
        val campoNombre = EditText(this)
        campoNombre.hint = "Nombre de la categoría"

        val colores = listOf(
            "Verde" to "#4CAF50",
            "Azul" to "#2196F3",
            "Naranja" to "#FF9800",
            "Rojo" to "#F44336",
            "Violeta" to "#9C27B0"
        )

        var colorSeleccionado = "#4CAF50"

        val botonColor = Button(this)
        botonColor.text = "Color: Verde"

        botonColor.setOnClickListener {
            val nombres = colores.map { it.first }.toTypedArray()

            AlertDialog.Builder(this)
                .setTitle("Seleccionar color")
                .setItems(nombres) { _, posicion ->
                    colorSeleccionado = colores[posicion].second
                    botonColor.text = "Color: ${colores[posicion].first}"
                }
                .show()
        }

        val contenedor = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            val margen = (16 * resources.displayMetrics.density).toInt()
            setPadding(margen, margen, margen, margen)

            addView(campoNombre)
            addView(botonColor)
        }

        val dialogo = AlertDialog.Builder(this)
            .setTitle("Nueva categoría")
            .setView(contenedor)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialogo.setOnShowListener {
            val botonGuardar = dialogo.getButton(AlertDialog.BUTTON_POSITIVE)

            botonGuardar.setOnClickListener {
                val nombre = campoNombre.text.toString().trim()

                if (nombre.isEmpty()) {
                    campoNombre.error = "Ingresá un nombre"
                } else {
                    val nuevaCategoria = Categoria(
                        nombre = nombre,
                        color = colorSeleccionado
                    )

                    botonGuardar.isEnabled = false

                    categoriaRepository.guardar(nuevaCategoria) { resultado ->
                        runOnUiThread {
                            resultado
                                .onSuccess {
                                    dialogo.dismiss()
                                    cargarCategorias()
                                }
                                .onFailure {
                                    botonGuardar.isEnabled = true
                                    Toast.makeText(
                                        this,
                                        "No se pudo guardar. Revisá si el nombre ya existe.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                    }
                }
            }
        }

        dialogo.show()
    }

    // Editar una categoría existente
    private fun editarCategoria(categoria: Categoria) {

        val campoNombre = EditText(this)
        campoNombre.setText(categoria.nombre)
        campoNombre.setSelection(campoNombre.text.length)
        val colores = listOf(
            "Verde" to "#4CAF50",
            "Azul" to "#2196F3",
            "Naranja" to "#FF9800",
            "Rojo" to "#F44336",
            "Violeta" to "#9C27B0"
        )

        var colorSeleccionado = categoria.color

        val botonColor = Button(this)

        val nombreColor = colores.firstOrNull {
            it.second.equals(colorSeleccionado, ignoreCase = true)
        }?.first ?: "Actual"

        botonColor.text = "Color: $nombreColor"


        botonColor.setOnClickListener {
            val nombres = colores.map { it.first }.toTypedArray()

            AlertDialog.Builder(this)
                .setTitle("Seleccionar color")
                .setItems(nombres) { _, posicion ->
                    colorSeleccionado = colores[posicion].second
                    botonColor.text = "Color: ${colores[posicion].first}"
                }
                .show()
        }

        val contenedor = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            val margen = (16 * resources.displayMetrics.density).toInt()
            setPadding(margen, margen, margen, margen)

            addView(campoNombre)
            addView(botonColor)
        }

        val dialogo = AlertDialog.Builder(this)
            .setTitle("Editar categoría")
            .setView(contenedor)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialogo.setOnShowListener {
            val botonGuardar = dialogo.getButton(AlertDialog.BUTTON_POSITIVE)

            botonGuardar.setOnClickListener {
                val nuevoNombre = campoNombre.text.toString().trim()

                if (nuevoNombre.isEmpty()) {
                    campoNombre.error = "Ingresá un nombre"
                } else {

                    val categoriaActualizada = categoria.copy(
                        nombre = nuevoNombre,
                        color = colorSeleccionado
                    )

                    botonGuardar.isEnabled = false

                    categoriaRepository.actualizar(categoriaActualizada) { resultado ->
                        runOnUiThread {
                            resultado
                                .onSuccess {
                                    dialogo.dismiss()
                                    cargarCategorias()
                                }
                                .onFailure {
                                    botonGuardar.isEnabled = true
                                    Toast.makeText(
                                        this,
                                        "No se pudo actualizar la categoría.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                    }
                }
            }
        }

        dialogo.show()
    }
    private fun eliminarCategoria(categoria: Categoria) {

        AlertDialog.Builder(this)
            .setTitle("Eliminar categoría")
            .setMessage(
                "¿Seguro que querés eliminar la categoría \"${categoria.nombre}\"?"
            )
            .setPositiveButton("Eliminar") { _, _ ->

                categoriaRepository.eliminar(categoria) { resultado ->

                    runOnUiThread {
                        resultado
                            .onSuccess {
                                cargarCategorias()
                                Toast.makeText(
                                    this,
                                    "Categoría eliminada correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .onFailure {
                                Toast.makeText(
                                    this,
                                    "No se pudo eliminar la categoría",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}