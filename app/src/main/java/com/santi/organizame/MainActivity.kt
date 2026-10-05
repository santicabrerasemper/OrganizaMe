package com.santi.organizame

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.santi.organizame.databinding.ActivityMainBinding
import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.EstadoTarea
import com.santi.organizame.modelo.Prioridad
import com.santi.organizame.modelo.TipoActividad

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val nuevaTareaLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->

            if (resultado.resultCode == RESULT_OK) {

                val datos = resultado.data

                val titulo = datos?.getStringExtra("titulo")
                val descripcion = datos?.getStringExtra("descripcion")
                val fecha = datos?.getStringExtra("fecha")
                val hora = datos?.getStringExtra("hora")
                val prioridad = datos?.getStringExtra("prioridad")
                val categoria = datos?.getStringExtra("categoria")
                val recordatorio = datos?.getStringExtra("recordatorio")
                val estado = datos?.getStringExtra("estado")


                binding.statusText.text = """
                    $titulo
                    $descripcion

                    Fecha: $fecha
                    Hora: $hora
                    Prioridad: $prioridad
                     Categoría: $categoria
                     Recordatorio: $recordatorio

                    Estado: $estado
                """.trimIndent()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tareaPrueba = Actividad(
            tipo = TipoActividad.TAREA,
            titulo = "Estudiar inglés",
            descripcion = "Repasar unidad 4",
            fecha = "05/10/2026",
            hora = "18:00",
            prioridad = Prioridad.ALTA,
            estadoTarea = EstadoTarea.PENDIENTE
        )

        binding.statusText.text = """
            ${tareaPrueba.titulo}
            ${tareaPrueba.descripcion}

            Fecha: ${tareaPrueba.fecha}
            Hora: ${tareaPrueba.hora}
            Prioridad: ${tareaPrueba.prioridad}
            Estado: ${tareaPrueba.estadoTarea}
        """.trimIndent()

        binding.btnNuevaTarea.setOnClickListener {

            val intent = Intent(this, NuevaTareaActividad::class.java)

            nuevaTareaLauncher.launch(intent)
        }
    }
}