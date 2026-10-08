package com.santi.organizame

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.santi.organizame.databinding.TarjetaActividadBinding
import com.santi.organizame.modelo.Actividad
import java.time.format.DateTimeFormatter

class AdaptadorTarjetasActividad(
    private var actividades: List<Actividad>,
    private val alEditar: (Actividad) -> Unit,
    private val alEliminar: (Actividad) -> Unit
) : RecyclerView.Adapter<AdaptadorTarjetasActividad.ActividadViewHolder>() {

    class ActividadViewHolder(
        val binding: TarjetaActividadBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ActividadViewHolder {

        val binding = TarjetaActividadBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ActividadViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ActividadViewHolder,
        position: Int
    ) {
        val actividad = actividades[position]

        holder.binding.txtTituloActividad.text = actividad.titulo

        holder.binding.txtDatosActividad.text = buildString {

            actividad.descripcion?.takeIf { it.isNotBlank() }?.let {
                append(it)
                append("\n")
            }

            actividad.fecha?.let {
                append("Fecha: ${it.format(FORMATO_FECHA)}\n")
            }

            actividad.hora?.let {
                append("Hora: ${it.format(FORMATO_HORA)}\n")
            }

            append("Prioridad: ${actividad.prioridad}\n")

            actividad.estadoTarea?.let {
                append("Estado: $it")
            }
        }

        holder.binding.btnEditarActividad.setOnClickListener {
            alEditar(actividad)
        }

        holder.binding.btnEliminarActividad.setOnClickListener {
            alEliminar(actividad)
        }
    }

    override fun getItemCount(): Int {
        return actividades.size
    }

    fun actualizarLista(nuevaLista: List<Actividad>) {
        actividades = nuevaLista
        notifyDataSetChanged()
    }

    companion object {
        private val FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu")

        private val FORMATO_HORA =
            DateTimeFormatter.ofPattern("HH:mm")
    }
}