package com.santi.organizame.modelo

data class Categoria (
    val id: Long = 0,
    val nombre: String,
    val color: String,
    val palabrasClave: List<String> = emptyList()

)