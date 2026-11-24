package com.santi.organizame.respaldo

import com.santi.organizame.modelo.Actividad
import com.santi.organizame.modelo.AnticipacionRecordatorio
import com.santi.organizame.modelo.Categoria
import com.santi.organizame.modelo.EstadoTarea
import com.santi.organizame.modelo.Prioridad
import com.santi.organizame.modelo.Recurrencia
import com.santi.organizame.modelo.TipoActividad
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

data class DatosRespaldo(
    val categorias: List<Categoria>,
    val actividades: List<Actividad>
)

object RespaldoJson {

    fun serializar(datos: DatosRespaldo): String {
        val raiz = JSONObject()
            .put("version", VERSION)
            .put("exportadoEn", Instant.now().toString())
            .put("categorias", JSONArray().apply {
                datos.categorias.forEach { categoria ->
                    put(JSONObject().apply {
                        put("id", categoria.id)
                        put("nombre", categoria.nombre)
                        put("color", categoria.color)
                        put("palabrasClave", JSONArray(categoria.palabrasClave))
                    })
                }
            })
            .put("actividades", JSONArray().apply {
                datos.actividades.forEach { actividad -> put(serializarActividad(actividad)) }
            })

        return raiz.toString(2)
    }

    fun deserializar(texto: String): DatosRespaldo {
        val raiz = JSONObject(texto)
        require(raiz.getInt("version") == VERSION) {
            "La versión del respaldo no es compatible"
        }

        val categoriasJson = raiz.getJSONArray("categorias")
        val categorias = buildList {
            for (indice in 0 until categoriasJson.length()) {
                val json = categoriasJson.getJSONObject(indice)
                val palabrasJson = json.optJSONArray("palabrasClave") ?: JSONArray()
                val palabras = buildList {
                    for (i in 0 until palabrasJson.length()) add(palabrasJson.getString(i))
                }
                add(
                    Categoria(
                        id = json.getLong("id"),
                        nombre = json.getString("nombre"),
                        color = json.getString("color"),
                        palabrasClave = palabras
                    )
                )
            }
        }

        val actividadesJson = raiz.getJSONArray("actividades")
        val actividades = buildList {
            for (indice in 0 until actividadesJson.length()) {
                add(deserializarActividad(actividadesJson.getJSONObject(indice)))
            }
        }

        require(categorias.all { it.id > 0L }) { "El respaldo contiene categorías inválidas" }
        require(actividades.all { it.id > 0L }) { "El respaldo contiene actividades inválidas" }

        return DatosRespaldo(categorias, actividades)
    }

    private fun serializarActividad(actividad: Actividad): JSONObject =
        JSONObject().apply {
            put("id", actividad.id)
            put("tipo", actividad.tipo.name)
            put("titulo", actividad.titulo)
            put("descripcion", actividad.descripcion ?: JSONObject.NULL)
            put("fecha", actividad.fecha?.toString() ?: JSONObject.NULL)
            put("hora", actividad.hora?.toString() ?: JSONObject.NULL)
            put("prioridad", actividad.prioridad.name)
            put("categoriaId", actividad.categoriaId ?: JSONObject.NULL)
            put("estadoTarea", actividad.estadoTarea?.name ?: JSONObject.NULL)
            put("fechaCompletada", actividad.fechaCompletada?.toString() ?: JSONObject.NULL)
            put("recordatorio", actividad.recordatorio?.name ?: JSONObject.NULL)
            put(
                "fechaRecordatorioProgramado",
                actividad.fechaRecordatorioProgramado?.toString() ?: JSONObject.NULL
            )
            put("recurrencia", actividad.recurrencia.name)
            put("recurrenciaProcesada", actividad.recurrenciaProcesada)
            put("fechaCreacion", actividad.fechaCreacion.toString())
            put("fechaModificacion", actividad.fechaModificacion.toString())
        }

    private fun deserializarActividad(json: JSONObject): Actividad =
        Actividad(
            id = json.getLong("id"),
            tipo = TipoActividad.valueOf(json.getString("tipo")),
            titulo = json.getString("titulo"),
            descripcion = json.textoOpcional("descripcion"),
            fecha = json.textoOpcional("fecha")?.let(LocalDate::parse),
            hora = json.textoOpcional("hora")?.let(LocalTime::parse),
            prioridad = Prioridad.valueOf(json.getString("prioridad")),
            categoriaId = json.longOpcional("categoriaId"),
            estadoTarea = json.textoOpcional("estadoTarea")?.let(EstadoTarea::valueOf),
            fechaCompletada = json.textoOpcional("fechaCompletada")?.let(Instant::parse),
            recordatorio = json.textoOpcional("recordatorio")
                ?.let(AnticipacionRecordatorio::valueOf),
            fechaRecordatorioProgramado =
                json.textoOpcional("fechaRecordatorioProgramado")?.let(Instant::parse),
            recurrencia = json.optString("recurrencia", Recurrencia.SIN_REPETICION.name)
                .let(Recurrencia::valueOf),
            recurrenciaProcesada = json.optBoolean("recurrenciaProcesada", false),
            fechaCreacion = Instant.parse(json.getString("fechaCreacion")),
            fechaModificacion = Instant.parse(json.getString("fechaModificacion"))
        )

    private fun JSONObject.textoOpcional(nombre: String): String? =
        if (!has(nombre) || isNull(nombre)) null else getString(nombre)

    private fun JSONObject.longOpcional(nombre: String): Long? =
        if (!has(nombre) || isNull(nombre)) null else getLong(nombre)

    private const val VERSION = 1
}
