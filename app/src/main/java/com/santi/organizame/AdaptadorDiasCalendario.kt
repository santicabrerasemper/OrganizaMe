package com.santi.organizame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.santi.organizame.databinding.ItemDiaCalendarioBinding

class AdaptadorDiasCalendario(
    private var dias: List<DiaCalendario>
) : RecyclerView.Adapter<AdaptadorDiasCalendario.DiaViewHolder>() {

    class DiaViewHolder(
        val binding: ItemDiaCalendarioBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DiaViewHolder {
        val binding = ItemDiaCalendarioBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DiaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiaViewHolder, position: Int) {
        val dia = dias[position]
        val binding = holder.binding

        if (dia.fecha == null) {
            binding.root.visibility = View.INVISIBLE
            return
        }

        binding.root.visibility = View.VISIBLE
        binding.textoNumeroDia.text = dia.fecha.dayOfMonth.toString()
        binding.indicadorActividad.visibility =
            if (dia.tieneActividades) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int = dias.size

    fun actualizarLista(nuevosDias: List<DiaCalendario>) {
        dias = nuevosDias
        notifyDataSetChanged()
    }
}