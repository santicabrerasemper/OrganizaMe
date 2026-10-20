package com.santi.organizame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.santi.organizame.modelo.Categoria

class AdaptadorTarjetasCategoria(
    private val alEditar: (Categoria) -> Unit,
    private val alEliminar: (Categoria) -> Unit
) : RecyclerView.Adapter<AdaptadorTarjetasCategoria.CategoriaViewHolder>() {

    private var categorias: List<Categoria> = emptyList()

    class CategoriaViewHolder(vista: View) :
        RecyclerView.ViewHolder(vista) {

        val nombreCategoria: TextView =
            vista.findViewById(R.id.nombreCategoria)

        val btnEditar: Button =
            vista.findViewById(R.id.btnEditarCategoria)

        val btnEliminar: Button =
            vista.findViewById(R.id.btnEliminarCategoria)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoriaViewHolder {

        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.tarjeta_categoria, parent, false)

        return CategoriaViewHolder(vista)
    }

    override fun onBindViewHolder(
        holder: CategoriaViewHolder,
        position: Int
    ) {
        val categoria = categorias[position]

        holder.nombreCategoria.text = categoria.nombre

        holder.btnEditar.setOnClickListener {
            alEditar(categoria)
        }

        holder.btnEliminar.setOnClickListener {
            alEliminar(categoria)
        }
    }

    override fun getItemCount(): Int {
        return categorias.size
    }

    fun actualizarLista(nuevasCategorias: List<Categoria>) {
        categorias = nuevasCategorias
        notifyDataSetChanged()
    }
}