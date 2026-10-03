package com.santi.organizame

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.santi.organizame.databinding.ActividadNuevaTareaBinding
import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.EstadoTarea
import com.santi.organizame.modelo.Prioridad
import com.santi.organizame.modelo.TipoActividad

class NuevaTareaActividad : AppCompatActivity() {

    private lateinit var binding: ActividadNuevaTareaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActividadNuevaTareaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGuardarTarea.setOnClickListener {

            val titulo = binding.tituloTarea.text.toString()
            val descripcion = binding.descripcionTarea.text.toString()
            val fecha = binding.fechaTarea.text.toString()
            val hora = binding.horaTarea.text.toString()

            val nuevaTarea = Actividad(
                tipo = TipoActividad.TAREA,
                titulo = titulo,
                descripcion = descripcion,
                fecha = fecha,
                hora = hora,
                prioridad = Prioridad.MEDIA,
                estadoTarea = EstadoTarea.PENDIENTE
            )

            val resultado = Intent()

            resultado.putExtra("titulo", nuevaTarea.titulo)
            resultado.putExtra("descripcion", nuevaTarea.descripcion)
            resultado.putExtra("fecha", nuevaTarea.fecha)
            resultado.putExtra("hora", nuevaTarea.hora)
            resultado.putExtra("prioridad", nuevaTarea.prioridad.name)
            resultado.putExtra("estado", nuevaTarea.estadoTarea?.name)

            setResult(RESULT_OK, resultado)

            finish()
        }
    }
}