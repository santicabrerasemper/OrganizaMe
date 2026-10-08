package com.santi.organizame.modelo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Actividad::class, Categoria::class],
    version = 1,
    exportSchema = false
)
abstract class OrganizaMeDatabase : RoomDatabase() {

    abstract fun actividadDao(): ActividadDao

    companion object {
        @Volatile
        private var instancia: OrganizaMeDatabase? = null

        fun obtener(context: Context): OrganizaMeDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    OrganizaMeDatabase::class.java,
                    "organizame.db"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)

                            db.execSQL(
                                "INSERT INTO categorias (id, nombre, color, palabrasClave) " +
                                        "VALUES (1, 'Personal', '#4CAF50', '')"
                            )
                            db.execSQL(
                                "INSERT INTO categorias (id, nombre, color, palabrasClave) " +
                                        "VALUES (2, 'Estudio', '#2196F3', '')"
                            )
                            db.execSQL(
                                "INSERT INTO categorias (id, nombre, color, palabrasClave) " +
                                        "VALUES (3, 'Trabajo', '#FF9800', '')"
                            )
                        }
                    })
                    .build()
                    .also { instancia = it }
            }
    }
}