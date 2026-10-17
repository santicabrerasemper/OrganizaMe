package com.santi.organizame

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import com.santi.organizame.modelo.CategoriaRepository
import com.santi.organizame.modelo.OrganizaMeDatabase

class CategoriasActividad : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.actividad_categorias)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val textoEstado = findViewById<TextView>(R.id.textoEstadoCategorias)

        val categoriaRepository = CategoriaRepository(
            OrganizaMeDatabase.obtener(applicationContext).categoriaDao()
        )

        categoriaRepository.obtenerTodas { resultado ->
            runOnUiThread {
                resultado
                    .onSuccess { categorias ->
                        textoEstado.text = if (categorias.isEmpty()) {
                            "No hay categorías guardadas"
                        } else {
                            categorias.joinToString("\n") { it.nombre }
                        }
                    }
                    .onFailure {
                        textoEstado.text = "Error al cargar las categorías"
                    }
            }
        }
    }


}