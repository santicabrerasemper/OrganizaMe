package com.santi.organizame.ui.lista

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.santi.organizame.databinding.TarjetaActividadBinding
import com.santi.organizame.modelo.Actividad
import java.time.format.DateTimeFormatter
import com.santi.organizame.modelo.EstadoTarea
import android.view.View
import java.time.LocalDateTime
import com.santi.organizame.modelo.TipoActividad
import com.santi.organizame.modelo.Recurrencia
import androidx.core.content.ContextCompat
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import java.time.temporal.IsoFields
import com.santi.organizame.R

class AdaptadorTarjetasActividad(
    private var actividades: List<Actividad>,
    private val alEditar: (Actividad) -> Unit,
    private val alEliminar: (Actividad) -> Unit,
    private val alCambiarEstado: (Actividad) -> Unit,
    private val alVerDetalle: (Actividad) -> Unit
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

        val fecha = actividad.fecha
        val esInicioDeGrupo =
            position == 0 || actividades[position - 1].fecha != fecha
        if (esInicioDeGrupo) {
            holder.binding.txtFechaGrupo.visibility = View.VISIBLE
            holder.binding.txtFechaGrupo.text =
                fecha?.format(FORMATO_FECHA) ?: "Sin fecha"
        } else {
            holder.binding.txtFechaGrupo.visibility = View.GONE
        }


        val ahora = LocalDateTime.now()

        val vencida = actividad.tipo == TipoActividad.TAREA &&
                actividad.estadoTarea == EstadoTarea.PENDIENTE &&
                actividad.fecha?.let { fecha ->
                    fecha.isBefore(ahora.toLocalDate()) ||
                            (fecha.isEqual(ahora.toLocalDate()) &&
                                    actividad.hora?.isBefore(ahora.toLocalTime()) == true)
                } == true



        val textoTitulo = if (vencida) {
            "⚠ VENCIDA · ${actividad.titulo}"
        } else {
            actividad.titulo
        }

        val tituloConColor = SpannableString(textoTitulo)

        if (vencida) {
            val inicio = textoTitulo.indexOf("VENCIDA")

            tituloConColor.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(holder.itemView.context, R.color.danger)
                ),
                inicio,
                inicio + "VENCIDA".length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        holder.binding.txtTituloActividad.text = tituloConColor


        val esTarea = actividad.tipo == TipoActividad.TAREA

        holder.binding.btnCompletarActividad.visibility =
            if (esTarea) View.VISIBLE else View.GONE

        if (esTarea){
            holder.binding.btnCompletarActividad.text =
                if (actividad.estadoTarea == EstadoTarea.PENDIENTE) {
                    "Completar"
                } else {
                    "Reabrir"
                }

            holder.binding.btnCompletarActividad.setOnClickListener {
                alCambiarEstado(actividad)
            }
        }

        holder.binding.txtDatosActividad.text = buildString {

            append("Tipo: ")
            append(if (actividad.tipo == TipoActividad.TAREA) "Tarea" else "Evento")
            append("\n")

            actividad.descripcion?.takeIf { it.isNotBlank() }?.let {
                append("Descripción: ")
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

            if (actividad.recurrencia != Recurrencia.SIN_REPETICION) {
                append(
                    "Repetición: " +
                            actividad.recurrencia.name.lowercase()
                                .replace('_', ' ') + "\n"
                )
            }

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

        holder.binding.btnVerDetalle.setOnClickListener {
            alVerDetalle(actividad)
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
