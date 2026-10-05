package com.santi.organizame

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.santi.organizame.databinding.ActividadNuevaTareaBinding
import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.EstadoTarea
import com.santi.organizame.modelo.Prioridad
import com.santi.organizame.modelo.TipoActividad
import android.widget.ArrayAdapter
import com.santi.organizame.modelo.Categoria
import com.santi.organizame.modelo.AnticipacionRecordatorio

class NuevaTareaActividad : AppCompatActivity() {

    private lateinit var binding: ActividadNuevaTareaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActividadNuevaTareaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prioridades = Prioridad.values().map { it.name }

        val adaptadorPrioridades = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            prioridades
        )

        adaptadorPrioridades.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        binding.prioridadTarea.adapter = adaptadorPrioridades

        binding.prioridadTarea.setSelection(
            Prioridad.values().indexOf(Prioridad.MEDIA)
        )

        val categorias = listOf(
            null,
            Categoria(1, "Personal", "#4CAF50"),
            Categoria(2, "Estudio", "#2196F3"),
            Categoria(3, "Trabajo", "#FF9800")
        )

        val nombresCategorias = categorias.map {
            it?.nombre ?: "Sin categoría"
        }

        val adaptadorCategorias = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            nombresCategorias
        )

        adaptadorCategorias.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        binding.categoriaTarea.adapter = adaptadorCategorias


        val opcionesRecordatorio = listOf(
            "Sin recordatorio",
            "A la hora",
            "10 minutos antes",
            "30 minutos antes",
            "1 hora antes",
            "1 día antes"
        )

        val adaptadorRecordatorio = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            opcionesRecordatorio
        )

        adaptadorRecordatorio.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        binding.recordatorioTarea.adapter = adaptadorRecordatorio


        binding.btnGuardarTarea.setOnClickListener {

            val titulo = binding.tituloTarea.text.toString()
            val descripcion = binding.descripcionTarea.text.toString()
            val fecha = binding.fechaTarea.text.toString()
            val hora = binding.horaTarea.text.toString()
            val prioridadSeleccionada =
                Prioridad.valueOf(binding.prioridadTarea.selectedItem.toString())
            val categoriaSeleccionada =
                categorias[binding.categoriaTarea.selectedItemPosition]

            val recordatorioSeleccionado =
                when (binding.recordatorioTarea.selectedItemPosition) {

                    1 -> AnticipacionRecordatorio.A_LA_HORA
                    2 -> AnticipacionRecordatorio.DIEZ_MINUTOS_ANTES
                    3 -> AnticipacionRecordatorio.TREINTA_MINUTOS_ANTES
                    4 -> AnticipacionRecordatorio.UNA_HORA_ANTES
                    5 -> AnticipacionRecordatorio.UN_DIA_ANTES

                    else -> null
                }

            // Validar título
            if (titulo.isBlank()) {
                binding.tituloTarea.error = "El título es obligatorio"
                return@setOnClickListener
            }

// Si hay hora, tiene que haber fecha
            if (hora.isNotBlank() && fecha.isBlank()) {
                binding.fechaTarea.error = "Tenés que ingresar una fecha"
                return@setOnClickListener
            }

// Si hay recordatorio, tiene que haber fecha y hora
            if (recordatorioSeleccionado != null &&
                (fecha.isBlank() || hora.isBlank())
            ) {
                binding.fechaTarea.error = "El recordatorio necesita fecha y hora"
                binding.horaTarea.error = "El recordatorio necesita fecha y hora"
                return@setOnClickListener
            }



            val nuevaTarea = Actividad(
                tipo = TipoActividad.TAREA,
                titulo = titulo,
                descripcion = descripcion,
                fecha = fecha,
                hora = hora,
                prioridad = prioridadSeleccionada,
                categoriaId = categoriaSeleccionada?.id,
                recordatorio = recordatorioSeleccionado,
                estadoTarea = EstadoTarea.PENDIENTE
            )

            val resultado = Intent()

            resultado.putExtra("titulo", nuevaTarea.titulo)
            resultado.putExtra("descripcion", nuevaTarea.descripcion)
            resultado.putExtra("fecha", nuevaTarea.fecha)
            resultado.putExtra("hora", nuevaTarea.hora)
            resultado.putExtra("prioridad", nuevaTarea.prioridad.name)
            resultado.putExtra(
                "categoria",
                categoriaSeleccionada?.nombre ?: "Sin categoría"
            )
            resultado.putExtra(
                "recordatorio",
                binding.recordatorioTarea.selectedItem.toString()
            )
            resultado.putExtra("estado", nuevaTarea.estadoTarea?.name)


            setResult(RESULT_OK, resultado)

            finish()
        }
    }
}