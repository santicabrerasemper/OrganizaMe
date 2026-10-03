package com.santi.organizame.modelo

data class Actividad (

    val id: Int = 0,
    val tipo: TipoActividad,
    val titulo: String,
    val descripcion: String? = null,
    val fecha: String? = null,
    val hora: String? = null,
    val prioridad: Prioridad = Prioridad.MEDIA,
    val estadoTarea: EstadoTarea? = null

)