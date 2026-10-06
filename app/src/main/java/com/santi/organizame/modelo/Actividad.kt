package com.santi.organizame.modelo

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

@Entity(
    tableName = "actividades",
    foreignKeys = [
        ForeignKey(
            entity = Categoria::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["categoriaId"]),
        Index(value = ["fecha", "hora"])
    ]
)
@TypeConverters(ConversoresRoom::class)
data class Actividad(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tipo: TipoActividad,
    val titulo: String,
    val descripcion: String? = null,
    val fecha: LocalDate? = null,
    val hora: LocalTime? = null,
    val prioridad: Prioridad = Prioridad.MEDIA,

    val categoriaId: Long? = null,

    val estadoTarea: EstadoTarea? = null,
    val fechaCompletada: Instant? = null,

    val recordatorio: AnticipacionRecordatorio? = null,
    val fechaRecordatorioProgramado: Instant? = null,

    val fechaCreacion: Instant = Instant.now(),
    val fechaModificacion: Instant = Instant.now()
)
