package com.santi.organizame.modelo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(
    tableName = "categorias",
    indices = [Index(value = ["nombre"], unique = true)]
)
@TypeConverters(ConversoresRoom::class)
data class Categoria(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val nombre: String,

    val color: String,
    val palabrasClave: List<String> = emptyList()
)
