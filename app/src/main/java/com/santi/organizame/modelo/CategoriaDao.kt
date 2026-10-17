package com.santi.organizame.modelo

import androidx.room.Update
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Dao
import androidx.room.Insert


    @Dao
    interface CategoriaDao {

        @Insert
        fun insertar(categoria: Categoria): Long

        @Update
        fun actualizar(categoria: Categoria)

        @Delete
        fun eliminar(categoria: Categoria)

        @Query("SELECT * FROM categorias ORDER BY nombre COLLATE NOCASE ASC")
        fun obtenerTodas(): List<Categoria>

        @Query("SELECT * FROM categorias WHERE id = :id LIMIT 1")
        fun obtenerPorId(id: Long): Categoria?
}
