package com.santi.organizame.modelo

import androidx.room.Update
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy


    @Dao
    interface CategoriaDao {

        @Insert
        fun insertar(categoria: Categoria): Long

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insertarTodas(categorias: List<Categoria>)

        @Update
        fun actualizar(categoria: Categoria)

        @Delete
        fun eliminar(categoria: Categoria)

        @Query("SELECT * FROM categorias ORDER BY nombre COLLATE NOCASE ASC")
        fun obtenerTodas(): List<Categoria>

        @Query("SELECT * FROM categorias WHERE id = :id LIMIT 1")
        fun obtenerPorId(id: Long): Categoria?

        @Query("DELETE FROM categorias")
        fun eliminarTodas()
}
