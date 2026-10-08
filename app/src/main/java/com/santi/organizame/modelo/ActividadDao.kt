package com.santi.organizame.modelo

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete

@Dao
interface ActividadDao {

    @Insert
    fun insertar(actividad: Actividad): Long

    @Update
    fun actualizar(actividad: Actividad)

    @Delete
    fun eliminar(actividad: Actividad)


    @Query(
        """
        SELECT * FROM actividades
        ORDER BY fecha IS NULL ASC, fecha ASC,
                 hora IS NULL ASC, hora ASC,
                 CASE prioridad
                     WHEN 'ALTA' THEN 3
                     WHEN 'MEDIA' THEN 2
                     ELSE 1
                 END DESC,
                 titulo COLLATE NOCASE ASC
        """
    )
    fun obtenerTodas(): List<Actividad>

    @Query("SELECT * FROM actividades WHERE id = :id LIMIT 1")
    fun obtenerPorId(id: Long): Actividad?
}