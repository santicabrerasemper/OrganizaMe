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
    abstract fun categoriaDao(): CategoriaDao

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
                                        "VALUES (1, 'Personal', '#0F766E', '')"
                            )
                            db.execSQL(
                                "INSERT INTO categorias (id, nombre, color, palabrasClave) " +
                                        "VALUES (2, 'Estudio', '#2563EB', '')"
                            )
                            db.execSQL(
                                "INSERT INTO categorias (id, nombre, color, palabrasClave) " +
                                        "VALUES (3, 'Trabajo', '#134E4A', '')"
                            )
                        }
                    })
                    .build()
                    .also { instancia = it }
            }
    }
}
