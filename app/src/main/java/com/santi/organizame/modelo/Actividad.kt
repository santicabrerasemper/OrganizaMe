package com.santi.organizame.modelo

data class Actividad (

    val id: Long = 0,
    val tipo: TipoActividad,
    val titulo: String,
    val descripcion: String? = null,
    val fecha: String? = null,
    val hora: String? = null,
    val prioridad: Prioridad = Prioridad.MEDIA,

    // Categoría
    val categoriaId: Long? = null,

    // Solo para tareas
    val estadoTarea: EstadoTarea? = null,
    val fechaCompletada: String? = null,

    // Recordatorio
    val recordatorio: AnticipacionRecordatorio? = null,
    val fechaRecordatorioProgramado: String? = null,

    // Auditoría
    val fechaCreacion: String? = null,
    val fechaModificacion: String? = null

)