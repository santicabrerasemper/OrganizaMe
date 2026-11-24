package com.santi.organizame.modelo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Actividad::class, Categoria::class],
    version = 2,
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
                    .addMigrations(MIGRACION_1_2)
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

        val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE actividades ADD COLUMN recurrencia " +
                            "TEXT NOT NULL DEFAULT 'SIN_REPETICION'"
                )
                db.execSQL(
                    "ALTER TABLE actividades ADD COLUMN recurrenciaProcesada " +
                            "INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
    }
}
