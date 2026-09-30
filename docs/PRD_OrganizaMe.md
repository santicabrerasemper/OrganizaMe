# PRD — OrganizaMe

**Producto:** OrganizaMe  
**Versión del documento:** 1.0  
**Estado:** Borrador listo para planificación  
**Plataforma:** Android nativo  
**Tipo de entrega:** Prototipo académico / proyecto final  
**Fecha:** 25 de septiembre de 2026

---

## 1. Resumen ejecutivo

OrganizaMe es una aplicación Android de uso individual para registrar, consultar y administrar tareas y eventos personales desde un único lugar. Permitirá asignar fecha, hora, prioridad, categoría y recordatorio; consultar las actividades en una agenda diaria o un calendario mensual; y recibir notificaciones locales.

El diferencial del producto será una carga rápida mediante texto en español. Un parser determinístico, basado en reglas y expresiones regulares, transformará instrucciones acotadas —por ejemplo, “estudiar mañana a las 18 prioridad alta”— en campos estructurados. La aplicación nunca guardará automáticamente lo interpretado: antes de crear la actividad mostrará una pantalla de confirmación con todos los campos editables y advertirá cuando un dato sea ambiguo o no haya podido interpretarse.

El MVP será local, funcionará sin conexión, no requerirá cuenta y estará diseñado para poder demostrar persistencia, arquitectura Android, procesos en segundo plano, notificaciones, pruebas automatizadas y evaluación cuantitativa del parser.

## 2. Problema

Las actividades personales suelen quedar repartidas entre notas, calendarios y alarmas. Esto provoca duplicación de carga, pérdida de contexto y dificultad para revisar qué está pendiente. Las aplicaciones que permiten escribir instrucciones libres suelen depender de servicios externos o modelos generativos, lo cual agrega costo, conexión obligatoria y resultados difíciles de reproducir.

OrganizaMe busca resolver ese problema con una experiencia centralizada y predecible:

- Una sola aplicación para tareas, eventos y recordatorios.
- Uso completamente local y sin conexión.
- Carga convencional mediante formulario y carga rápida mediante texto.
- Interpretación limitada, documentada y verificable.
- Confirmación humana antes de persistir cualquier resultado del parser.

## 3. Objetivos del producto

### 3.1 Objetivo general

Desarrollar y evaluar un prototipo Android offline-first que permita planificar tareas y eventos mediante formularios y mediante la interpretación controlada de instrucciones escritas en español.

### 3.2 Objetivos específicos

1. Implementar el ciclo completo de alta, consulta, modificación, finalización y eliminación de actividades.
2. Centralizar tareas y eventos en una agenda diaria y un calendario mensual.
3. Programar recordatorios locales que se actualicen o cancelen junto con la actividad.
4. Interpretar un conjunto explícito de expresiones temporales, prioridades y categorías en español.
5. Evitar guardados incorrectos mediante una pantalla de revisión obligatoria.
6. Medir el comportamiento del parser con un corpus reproducible de al menos 30 frases.
7. Demostrar separación de responsabilidades mediante una arquitectura MVVM con repositorios y casos de uso.

### 3.3 Indicadores de éxito del MVP

El MVP se considerará exitoso cuando cumpla todos los siguientes indicadores:

- El 100 % de los flujos críticos de actividad puede completarse sin conexión.
- Crear, editar, completar y eliminar tareas funciona sin pérdida ni duplicación de datos en las pruebas de aceptación.
- Crear, editar y eliminar eventos funciona sin pérdida ni duplicación de datos.
- La agenda y el calendario reflejan los cambios persistidos al volver a abrir la aplicación.
- Los recordatorios se programan, sustituyen y cancelan de acuerdo con las reglas del producto.
- El parser reconoce correctamente al menos el 80 % de los campos esperados en las frases válidas del corpus acordado.
- El parser identifica como “requiere revisión” el 100 % de los casos ambiguos definidos en el corpus.
- Ninguna interpretación se guarda sin confirmación explícita del usuario.
- No se produce ningún cierre inesperado en el recorrido de demostración acordado.

La precisión del parser se medirá por campo —título, tipo, fecha, hora, prioridad y categoría— y también por coincidencia exacta de la actividad completa. Ambos resultados deberán informarse por separado.

## 4. Alcance

### 4.1 Incluido en el MVP

- Uso individual, sin registro ni inicio de sesión.
- Persistencia local en el dispositivo.
- Gestión de tareas y eventos.
- Categorías configurables.
- Prioridades baja, media y alta.
- Estados de tarea pendiente y completada.
- Búsqueda y filtros por texto, tipo, estado, categoría y prioridad.
- Agenda de actividades por día.
- Calendario mensual con detalle del día seleccionado.
- Recordatorios mediante notificaciones locales.
- Ingreso rápido mediante parser de reglas para frases en español.
- Confirmación y corrección de los campos extraídos.
- Datos de demostración reproducibles.
- Corpus y reporte de evaluación del parser.
- Pruebas unitarias de dominio/parser y pruebas de los flujos críticos.

### 4.2 Fuera de alcance

- Asistente conversacional o comprensión irrestricta del lenguaje.
- Modelos generativos o dependencia obligatoria de APIs externas.
- Voz, dictado o procesamiento de audio.
- Sincronización entre dispositivos.
- Cuentas, autenticación, perfiles múltiples o colaboración.
- Integración con Google Calendar, Outlook u otros calendarios.
- Aplicación web, versión iOS o widgets.
- Ubicación, contactos o archivos adjuntos.
- Reglas complejas de recurrencia.
- Respaldo en la nube.
- Validación comercial o de mercado.

### 4.3 Extensiones posteriores al MVP

- Tareas recurrentes con reglas simples.
- Exportación e importación de un respaldo local.
- Resumen semanal de pendientes.
- Fecha y hora de finalización para eventos.
- Preferencias avanzadas de notificación.

## 5. Usuarios y contexto de uso

### 5.1 Usuario principal

Persona que necesita organizar estudio, trabajo y actividades personales desde su teléfono, valora una carga rápida y quiere consultar sus pendientes sin depender de una conexión.

### 5.2 Necesidades principales

- Registrar una actividad en pocos pasos.
- Saber qué debe hacer hoy o en una fecha determinada.
- Recibir un aviso a tiempo.
- Corregir fácilmente una interpretación incorrecta.
- Encontrar una actividad por nombre, categoría, prioridad o estado.

### 5.3 Supuestos de contexto

- Existe un único usuario por instalación.
- La zona horaria y el idioma se toman del dispositivo; la interfaz y las reglas del parser se diseñan para español.
- El usuario puede denegar permisos de notificaciones. En ese caso, la actividad se guarda, pero la aplicación informa que el recordatorio no podrá mostrarse.
- El reloj del dispositivo es la fuente de verdad para fechas y horarios.

## 6. Definiciones y reglas de negocio

### 6.1 Actividad

“Actividad” es el concepto común para una tarea o un evento.

### 6.2 Tarea

- Tiene título obligatorio.
- Puede tener descripción, fecha, hora, prioridad, categoría y recordatorio.
- Su estado es `pendiente` o `completada`.
- Si posee hora, también debe poseer fecha.
- Completar una tarea no la elimina y debe cancelar cualquier recordatorio futuro asociado.
- Reabrir una tarea permite volver a programar su recordatorio si este sigue siendo futuro.

### 6.3 Evento

- Tiene título y fecha de inicio obligatorios.
- Puede tener descripción, hora, prioridad, categoría y recordatorio.
- No posee estado de completado en el MVP.
- Un evento sin hora se considera de día completo y no puede generar un recordatorio con hora hasta que el usuario la asigne.

### 6.4 Prioridad

- Valores disponibles: `baja`, `media` y `alta`.
- El valor inicial de una nueva actividad es `media`.
- La prioridad afecta el orden y la presentación, pero no modifica recordatorios automáticamente.

### 6.5 Categoría

- Una actividad puede tener cero o una categoría.
- El usuario puede crear, renombrar y eliminar categorías.
- Cada categoría tiene nombre único, color y palabras clave opcionales para el parser.
- Al eliminar una categoría, las actividades asociadas se conservan y quedan sin categoría.
- Se incluyen categorías de ejemplo —Personal, Estudio y Trabajo— que el usuario puede modificar o eliminar.

### 6.6 Recordatorio

- Solo puede programarse si la actividad posee fecha y hora.
- Debe señalar un instante futuro al momento de guardar.
- Para el MVP se admite un único recordatorio por actividad.
- El usuario selecciona el momento del recordatorio entre opciones predefinidas: a la hora, 10 minutos antes, 30 minutos antes, 1 hora antes o 1 día antes.
- Si el instante calculado ya pasó, la aplicación no lo programa y muestra una validación.
- Editar la fecha, hora o anticipación reemplaza el recordatorio anterior.
- Eliminar una actividad o completar una tarea cancela el recordatorio asociado.

### 6.7 Eliminación

- Antes de eliminar una actividad se solicita confirmación.
- La eliminación es definitiva dentro del MVP.
- La cancelación del recordatorio forma parte de la misma operación lógica.

## 7. Experiencia y navegación

### 7.1 Navegación principal

La aplicación tendrá cuatro destinos principales:

1. **Agenda:** actividades agrupadas por día, con acceso rápido a hoy.
2. **Calendario:** mes visible y lista de actividades del día seleccionado.
3. **Nueva actividad:** elección entre formulario manual e ingreso por texto.
4. **Categorías/Ajustes:** administración de categorías y acceso a información sobre las frases soportadas.

### 7.2 Pantallas mínimas

- Agenda diaria.
- Calendario mensual.
- Detalle de actividad.
- Formulario de tarea/evento.
- Entrada de frase.
- Confirmación de interpretación.
- Búsqueda y filtros.
- Gestión de categorías.
- Ayuda del parser con ejemplos y limitaciones.

### 7.3 Estados de interfaz obligatorios

Cada listado debe contemplar:

- Cargando, cuando corresponda.
- Con resultados.
- Sin actividades.
- Sin coincidencias para búsqueda/filtros.
- Error recuperable.

Los formularios deben indicar el campo inválido y la acción necesaria para corregirlo.

## 8. Flujos críticos

### 8.1 Crear una actividad manualmente

1. El usuario pulsa “Nueva actividad”.
2. Selecciona “Carga manual”.
3. Elige tarea o evento.
4. Completa los campos.
5. La aplicación valida los datos.
6. Si existe recordatorio, valida permiso e instante futuro.
7. Guarda la actividad y programa el recordatorio.
8. Muestra la actividad en agenda/calendario y confirma el resultado.

### 8.2 Crear mediante texto

1. El usuario pulsa “Nueva actividad”.
2. Selecciona “Escribir instrucción”.
3. Ingresa una frase y solicita interpretarla.
4. El parser normaliza y extrae campos.
5. La aplicación muestra el formulario de confirmación con los valores extraídos.
6. Los campos inciertos, faltantes o conflictivos aparecen destacados.
7. El usuario corrige o completa la información.
8. El usuario confirma el guardado.
9. La aplicación persiste y, si corresponde, programa el recordatorio.

### 8.3 Editar una actividad con recordatorio

1. El usuario abre el detalle y selecciona “Editar”.
2. Modifica fecha, hora o recordatorio.
3. La aplicación valida los cambios.
4. Cancela la programación anterior y crea la nueva usando el mismo identificador lógico.
5. Persiste la edición y actualiza las vistas.

### 8.4 Completar una tarea

1. El usuario marca una tarea pendiente como completada.
2. La aplicación registra el nuevo estado y la fecha de finalización.
3. Cancela el recordatorio futuro, si existe.
4. Actualiza la agenda y mantiene la tarea disponible al aplicar el filtro “Completadas”.

### 8.5 Eliminar una actividad

1. El usuario solicita eliminarla.
2. La aplicación pide confirmación.
3. Al confirmar, cancela el recordatorio asociado.
4. Elimina el registro y actualiza todas las vistas.

## 9. Requisitos funcionales

### 9.1 Actividades

| ID | Requisito | Prioridad |
|---|---|---|
| RF-A01 | Crear una tarea con título obligatorio y campos opcionales. | P0 |
| RF-A02 | Crear un evento con título y fecha obligatorios. | P0 |
| RF-A03 | Consultar el detalle completo de una actividad. | P0 |
| RF-A04 | Editar cualquiera de los campos permitidos. | P0 |
| RF-A05 | Eliminar una actividad después de una confirmación. | P0 |
| RF-A06 | Marcar una tarea como completada y volver a abrirla. | P0 |
| RF-A07 | Validar relaciones entre fecha, hora y recordatorio antes de guardar. | P0 |
| RF-A08 | Persistir los cambios entre cierres y reinicios de la aplicación. | P0 |

### 9.2 Agenda, calendario, búsqueda y filtros

| ID | Requisito | Prioridad |
|---|---|---|
| RF-V01 | Mostrar una agenda agrupada por fecha. | P0 |
| RF-V02 | Mostrar las actividades sin fecha en una sección “Sin fecha”. | P0 |
| RF-V03 | Mostrar un calendario mensual con indicador en los días que tienen actividades. | P0 |
| RF-V04 | Al seleccionar un día, listar sus tareas y eventos. | P0 |
| RF-V05 | Buscar por coincidencia parcial del título o descripción, sin distinguir mayúsculas ni tildes. | P0 |
| RF-V06 | Filtrar por tipo, estado, categoría y prioridad. | P0 |
| RF-V07 | Combinar filtros usando condición AND. | P0 |
| RF-V08 | Permitir ordenar por fecha/hora o prioridad. | P1 |
| RF-V09 | Los filtros solo afectan la visualización y no modifican datos. | P0 |

Orden predeterminado: fecha ascendente, hora ascendente, prioridad descendente y título ascendente como desempate. Las actividades sin hora aparecen después de las actividades con hora del mismo día.

### 9.3 Categorías

| ID | Requisito | Prioridad |
|---|---|---|
| RF-C01 | Crear una categoría con nombre, color y palabras clave opcionales. | P0 |
| RF-C02 | Editar una categoría existente. | P0 |
| RF-C03 | Eliminar una categoría sin eliminar sus actividades. | P0 |
| RF-C04 | Impedir nombres de categoría vacíos o duplicados sin distinguir mayúsculas. | P0 |
| RF-C05 | Usar las palabras clave configuradas durante la interpretación. | P0 |

### 9.4 Recordatorios

| ID | Requisito | Prioridad |
|---|---|---|
| RF-R01 | Solicitar o explicar el permiso de notificaciones cuando sea necesario. | P0 |
| RF-R02 | Programar un recordatorio local para una actividad válida. | P0 |
| RF-R03 | Mostrar título, tipo y fecha/hora de la actividad en la notificación. | P0 |
| RF-R04 | Abrir el detalle correspondiente al tocar la notificación. | P1 |
| RF-R05 | Reprogramar el aviso al editar datos relevantes. | P0 |
| RF-R06 | Cancelar el aviso al eliminar la actividad o completar una tarea. | P0 |
| RF-R07 | Recuperar recordatorios futuros después de reiniciar el dispositivo, si la plataforma lo requiere. | P1 |
| RF-R08 | Informar claramente cuando el permiso esté denegado; la actividad igualmente debe poder guardarse. | P0 |

### 9.5 Parser y confirmación

| ID | Requisito | Prioridad |
|---|---|---|
| RF-P01 | Aceptar una instrucción escrita no vacía. | P0 |
| RF-P02 | Normalizar mayúsculas, espacios y variantes con/sin tilde para comparar palabras clave. | P0 |
| RF-P03 | Extraer fechas absolutas en los formatos documentados. | P0 |
| RF-P04 | Extraer hoy, mañana y pasado mañana. | P0 |
| RF-P05 | Resolver días de la semana según la regla de próxima ocurrencia. | P0 |
| RF-P06 | Extraer horarios de 24 horas. | P0 |
| RF-P07 | Extraer prioridad mediante palabras clave. | P0 |
| RF-P08 | Extraer categoría usando nombre o palabras clave configuradas. | P0 |
| RF-P09 | Construir el título a partir del texto restante. | P0 |
| RF-P10 | Marcar campos ambiguos, inválidos o conflictivos. | P0 |
| RF-P11 | Mostrar siempre una pantalla editable de confirmación. | P0 |
| RF-P12 | No guardar ninguna actividad desde el parser sin acción explícita del usuario. | P0 |
| RF-P13 | Conservar la frase original para mostrarla durante la revisión, sin obligación de persistirla después. | P1 |

## 10. Especificación del parser

### 10.1 Principios

- Es determinístico: la misma frase, fecha/hora base y configuración deben producir el mismo resultado.
- Es acotado: solo interpreta expresiones documentadas.
- Es tolerante: una extracción parcial puede pasar a confirmación.
- Es seguro: ante conflicto o ambigüedad no elige silenciosamente; marca revisión.
- No persiste datos ni programa recordatorios por sí mismo.

### 10.2 Entrada y salida

Entrada:

- Frase original.
- Fecha y hora actual del dispositivo.
- Zona horaria.
- Lista de categorías y palabras clave.

Salida lógica sugerida:

```text
ParseResult
  originalText
  title: value + confidence/status
  activityType: TASK | EVENT + status
  date: LocalDate? + status
  time: LocalTime? + status
  priority: LOW | MEDIUM | HIGH + status
  categoryId: Id? + status
  warnings: List<ParseWarning>
  remainingText
```

Los estados posibles de un campo son `EXTRACTED`, `DEFAULTED`, `MISSING`, `AMBIGUOUS` o `INVALID`.

### 10.3 Expresiones soportadas

| Campo | Expresiones mínimas | Ejemplos |
|---|---|---|
| Fecha relativa | hoy, mañana, pasado mañana | “comprar mañana” |
| Día semanal | lunes a domingo; opcionalmente “el” | “reunión el viernes” |
| Fecha absoluta | `dd/MM/yyyy`, `dd-MM-yyyy`; año opcional si se documenta y prueba | “turno 15/10/2026” |
| Hora | `HH:mm`, `H:mm`, “a las H”, “a las H:mm” | “estudiar a las 18” |
| Prioridad | prioridad alta/media/baja, urgente como alias de alta | “entregar informe urgente” |
| Categoría | nombre o palabra clave de una categoría | “estudiar álgebra facultad” |
| Tipo | palabras explícitas como “tarea” o “evento”; alias acotados documentados | “evento reunión…” |
| Título | texto significativo restante luego de extraer tokens | “estudiar álgebra” |

### 10.4 Resolución temporal

- `hoy`, `mañana` y `pasado mañana` se calculan desde la fecha local del dispositivo.
- Un día semanal se resuelve hacia su próxima ocurrencia. Si coincide con el día actual y la hora extraída todavía no pasó, se usa hoy; si la hora ya pasó, se usa la semana siguiente.
- Si el día coincide con hoy y no existe hora, se usa hoy y se marca la fecha para revisión en la confirmación.
- Una fecha sin año usa el año actual si todavía no pasó; en caso contrario usa el año siguiente. Esta regla debe explicarse en la ayuda.
- Una hora aislada no agrega fecha automáticamente: se extrae la hora y se marca la fecha como faltante.
- Una fecha u hora inexistente —por ejemplo, 31/02 o 25:70— se marca como inválida y no se convierte silenciosamente.
- Si aparecen dos fechas u horas distintas, el campo se marca como ambiguo.

### 10.5 Tipo predeterminado

- Si la frase contiene una palabra clave explícita de tipo, se usa ese tipo.
- Si no existe palabra clave de tipo, el resultado se propone como tarea con estado `DEFAULTED` y queda visible para corrección.
- Los alias de evento del MVP serán acotados y documentados —por ejemplo, reunión, cita y turno—. No se inferirá el tipo por contexto fuera de esa lista.

### 10.6 Orden sugerido de procesamiento

1. Conservar la frase original.
2. Normalizar una copia para comparación.
3. Detectar conflictos y expresiones no válidas conocidas.
4. Extraer fecha.
5. Extraer hora.
6. Extraer tipo.
7. Extraer prioridad.
8. Extraer categoría.
9. Limpiar conectores residuales controlados.
10. Construir y validar el título restante.
11. Generar advertencias y devolver el resultado.

### 10.7 Ejemplos esperados

| Frase | Resultado esperado |
|---|---|
| “estudiar mañana a las 18” | Tarea; título “estudiar”; mañana; 18:00; prioridad media por defecto. |
| “reunión el viernes a las 10 prioridad alta” | Evento por alias; título “reunión”; próximo viernes; 10:00; alta. |
| “tarea comprar remedios hoy urgente” | Tarea; título “comprar remedios”; hoy; alta. |
| “turno dentista 15/10/2026 a las 09:30” | Evento; título “dentista”; 15/10/2026; 09:30. |
| “estudiar a las 18 o a las 20” | Hora ambigua; requiere corrección. |
| “pagar factura 31/02/2027” | Fecha inválida; no permite confirmar hasta corregirla o quitarla. |
| “hacer algo la semana que viene” | Expresión no soportada; se conserva como título o se advierte según la regla definida. |

## 11. Criterios de aceptación por historia de usuario

### HU-01 — Crear una tarea manual

**Como** usuario, **quiero** crear una tarea desde un formulario **para** registrar un pendiente.

Criterios:

- Dado un título válido, al guardar se crea una única tarea.
- Si el título está vacío, se informa el error y no se guarda.
- Si existe hora sin fecha, se informa el error y no se guarda hasta corregirlo.
- La tarea recién creada aparece en la agenda correspondiente sin reiniciar la aplicación.
- Al cerrar y volver a abrir la aplicación, la tarea continúa disponible.

### HU-02 — Crear un evento manual

**Como** usuario, **quiero** crear un evento **para** recordar una actividad en una fecha.

Criterios:

- Título y fecha son obligatorios.
- Un evento sin hora se guarda como evento de día completo.
- El evento aparece en el día correcto del calendario.
- Una fecha inválida impide el guardado y muestra un mensaje accionable.

### HU-03 — Crear desde una frase

**Como** usuario, **quiero** escribir una instrucción simple **para** cargar una actividad con menos pasos.

Criterios:

- Una frase válida produce una vista previa con los campos extraídos.
- La vista previa permite cambiar tipo, título, fecha, hora, prioridad y categoría.
- Los campos ambiguos o inválidos se distinguen visualmente y explican el problema.
- Salir de la confirmación sin guardar no crea ninguna actividad.
- Confirmar una vista previa válida crea una sola actividad.

### HU-04 — Editar y reprogramar

**Como** usuario, **quiero** editar una actividad **para** mantenerla actualizada.

Criterios:

- Los valores actuales aparecen precargados.
- Guardar cambios actualiza agenda, calendario y detalle.
- Si cambia un dato del recordatorio, la programación anterior queda cancelada y existe solo la nueva.
- Cancelar la edición no modifica la actividad.

### HU-05 — Completar una tarea

**Como** usuario, **quiero** marcar una tarea como completada **para** diferenciarla de mis pendientes.

Criterios:

- La tarea deja de aparecer con el filtro “Pendientes”.
- Aparece con el filtro “Completadas”.
- Su recordatorio futuro queda cancelado.
- Al reabrirla, vuelve a estado pendiente; si su recordatorio original ya pasó, no se programa automáticamente.

### HU-06 — Eliminar una actividad

**Como** usuario, **quiero** eliminar una actividad **para** quitar información que ya no necesito.

Criterios:

- La aplicación pide confirmación.
- Cancelar mantiene la actividad sin cambios.
- Confirmar elimina la actividad de todas las vistas y cancela su recordatorio.

### HU-07 — Consultar y filtrar

**Como** usuario, **quiero** buscar y filtrar actividades **para** encontrar rápidamente lo que necesito.

Criterios:

- La búsqueda parcial funciona sin distinguir mayúsculas ni tildes.
- Los filtros seleccionados pueden combinarse.
- Una consulta sin coincidencias muestra un estado vacío específico.
- Quitar filtros restaura el conjunto completo sin modificar datos.

## 12. Modelo de datos conceptual

### Activity

| Campo | Tipo conceptual | Regla |
|---|---|---|
| id | UUID/Long | Identificador estable y único. |
| type | TASK / EVENT | Obligatorio. |
| title | String | Obligatorio; texto no vacío. |
| description | String? | Opcional. |
| date | LocalDate? | Obligatoria para eventos; opcional para tareas. |
| time | LocalTime? | Requiere fecha. |
| priority | LOW / MEDIUM / HIGH | Obligatoria; media por defecto. |
| categoryId | FK? | Opcional. |
| taskStatus | PENDING / COMPLETED / null | Solo para tareas. |
| completedAt | Instant? | Solo cuando la tarea está completada. |
| reminderOffset | Enum? | Requiere fecha y hora. |
| reminderScheduledAt | Instant? | Instante calculado. |
| createdAt | Instant | Auditoría local. |
| updatedAt | Instant | Auditoría local. |

### Category

| Campo | Tipo conceptual | Regla |
|---|---|---|
| id | UUID/Long | Identificador único. |
| name | String | Obligatorio y único sin distinguir mayúsculas. |
| color | String/Int | Color válido de la paleta. |
| keywords | List<String> | Cero o más términos únicos normalizados. |

Las palabras clave pueden almacenarse en una tabla relacionada o mediante un conversor, según el diseño técnico. La elección debe favorecer consultas y pruebas simples.

## 13. Arquitectura y restricciones técnicas

### 13.1 Stack propuesto

- Kotlin.
- Android nativo con Activities/Fragments y layouts XML, de acuerdo con la propuesta académica.
- MVVM para presentación.
- Casos de uso para reglas de negocio.
- Repositorios como límite entre dominio y fuentes de datos.
- Room sobre SQLite para persistencia local.
- WorkManager para trabajo diferible y AlarmManager cuando se requiera una alarma puntual.
- API de fecha/hora de Java/Kotlin y expresiones regulares para el parser.
- Pruebas unitarias con el framework adoptado por la cátedra/equipo y pruebas instrumentadas para integración Android.

### 13.2 Separación mínima de componentes

```text
UI (Fragments / ViewModels)
        ↓
Casos de uso
        ↓
Repositorio de actividades ── Room
        ├───────────────────── Programador de recordatorios
        └───────────────────── Parser (servicio de dominio puro)
```

El parser no debe depender de Android ni de la base de datos. Debe recibir fecha/hora base y categorías mediante parámetros para que sus pruebas sean determinísticas.

### 13.3 Compatibilidad

- La versión mínima de Android debe confirmarse con los requisitos de la facultad. Se recomienda elegir una versión que permita usar `java.time` sin complejidad adicional y documentar la decisión.
- La versión objetivo y las dependencias se fijarán al iniciar el repositorio para asegurar builds reproducibles.

## 14. Requisitos no funcionales

| ID | Requisito |
|---|---|
| RNF-01 | Todas las funciones P0, salvo servicios propios del sistema operativo, deben operar sin conexión. |
| RNF-02 | La aplicación no debe enviar actividades, categorías ni frases a servicios externos. |
| RNF-03 | Las operaciones habituales sobre datos locales deben reflejarse en pantalla en menos de 500 ms en el dispositivo de prueba acordado. |
| RNF-04 | El parser debe devolver un resultado en menos de 200 ms para una frase de hasta 200 caracteres en el dispositivo de prueba. |
| RNF-05 | Los datos deben mantenerse después de cerrar la aplicación o reiniciar el dispositivo. |
| RNF-06 | Los errores de validación deben indicar qué campo corregir, sin perder los valores ingresados. |
| RNF-07 | Controles, contraste y tamaños táctiles deben seguir pautas básicas de accesibilidad Android. |
| RNF-08 | Fechas y horas deben presentarse con formato coherente con español y zona horaria local. |
| RNF-09 | El código de dominio crítico debe contar con pruebas unitarias. |
| RNF-10 | La aplicación no debe registrar en logs de producción el contenido completo de las actividades. |
| RNF-11 | El proyecto debe poder compilarse desde cero mediante instrucciones documentadas. |

Los límites de rendimiento son objetivos de aceptación para el prototipo, no garantías para todos los dispositivos Android.

## 15. Evaluación del parser

### 15.1 Corpus

Se construirá un conjunto versionado de al menos 30 frases, dividido como mínimo en:

- 15 frases válidas soportadas.
- 8 frases ambiguas o con conflicto.
- 7 frases inválidas o no soportadas.

Cada caso debe incluir:

- Identificador.
- Frase.
- Fecha y hora base.
- Categorías/palabras clave disponibles.
- Campos esperados.
- Advertencias esperadas.
- Clasificación: válida, ambigua o no soportada.

### 15.2 Métricas

- Exactitud por campo = campos correctos / campos evaluados.
- Exactitud completa = frases con todos los campos correctos / frases válidas.
- Detección de ambigüedad = ambiguas marcadas / casos ambiguos.
- Rechazo controlado = casos inválidos que generan advertencia y no cierre inesperado / casos inválidos.

El informe final debe mostrar una matriz por caso y un resumen de errores frecuentes. Una frase parcialmente interpretada no cuenta como coincidencia completa, aunque sus campos acertados sí cuentan en la métrica por campo.

## 16. Estrategia de pruebas

### 16.1 Pruebas unitarias

- Todas las reglas del parser con reloj fijo.
- Conversión de fecha relativa y día semanal.
- Validación de fechas y horas.
- Normalización de tildes y mayúsculas.
- Detección de categorías y conflictos de palabras clave.
- Reglas de tareas/eventos y recordatorios.
- Ordenamiento y combinación de filtros.

### 16.2 Pruebas de integración

- DAO de Room: crear, consultar, editar y eliminar.
- Repositorio y casos de uso.
- Programar, reemplazar y cancelar recordatorios usando abstracciones verificables.
- Migración de base de datos, si la versión del esquema cambia durante el proyecto.

### 16.3 Pruebas de interfaz/aceptación

- Crear actividad manual.
- Crear desde frase y corregir datos.
- Editar actividad con recordatorio.
- Completar y reabrir tarea.
- Eliminar actividad.
- Buscar y combinar filtros.
- Navegar entre agenda y calendario.
- Denegar permiso de notificaciones sin impedir el guardado.

### 16.4 Prueba exploratoria de usabilidad

Realizar una sesión breve con participantes disponibles, usando tareas guiadas. Registrar tiempo, errores, dudas y comentarios; no presentar el resultado como validación de mercado. Recorridos sugeridos:

1. Crear manualmente una tarea para mañana.
2. Crear un evento mediante una frase.
3. Corregir una interpretación ambigua.
4. Encontrar y completar una tarea.
5. Cambiar un recordatorio y eliminar otra actividad.

## 17. Datos de demostración

El proyecto debe incluir un mecanismo de carga reproducible para el entorno de demo, separado de los datos reales. Conjunto sugerido:

- Dos tareas pendientes para hoy.
- Una tarea completada.
- Una tarea futura de prioridad alta con recordatorio.
- Un evento de día completo.
- Un evento con hora y recordatorio.
- Una actividad sin fecha.
- Tres categorías con palabras clave.

La carga de demo no debe duplicarse cada vez que se inicia la aplicación.

## 18. Roadmap de implementación

### Fase 0 — Alineación y diseño

- Confirmar requisitos de la facultad, versión mínima de Android y formato de entrega.
- Validar reglas de negocio y gramática soportada.
- Crear wireframes de las pantallas mínimas.
- Definir esquema de datos y contratos de dominio.
- Preparar repositorio, ramas, convenciones y CI si corresponde.

**Salida:** decisiones cerradas, backlog inicial y proyecto compilable.

### Fase 1 — Núcleo local

- Entidades, DAO, Room y repositorios.
- Casos de uso de tareas, eventos y categorías.
- Formulario manual y detalle.
- Agenda básica.
- Pruebas unitarias y de persistencia.

**Salida:** ciclo CRUD local funcional.

### Fase 2 — Consulta y calendario

- Agenda agrupada.
- Calendario mensual.
- Búsqueda, filtros y estados vacíos.
- Completar/reabrir tareas.

**Salida:** organización y consulta completas.

### Fase 3 — Recordatorios

- Permisos y canal de notificaciones.
- Programación local.
- Reprogramación y cancelación.
- Pruebas de reglas y recorrido manual en dispositivo.

**Salida:** recordatorios consistentes con el ciclo de vida de la actividad.

### Fase 4 — Parser

- Normalización y reglas temporales.
- Prioridad, categoría, tipo y título.
- Modelo de ambigüedad/advertencias.
- Pantalla de confirmación.
- Corpus automatizado y reporte inicial.

**Salida:** carga por texto medible y corregible.

### Fase 5 — Integración y entrega

- Datos de demostración.
- Pruebas end-to-end de flujos críticos.
- Accesibilidad y mensajes de error.
- Corrección de defectos.
- Documentación técnica, manual breve y reporte de evaluación.
- Ensayo del escenario de presentación.

**Salida:** versión candidata para la entrega académica.

## 19. Priorización del backlog

### P0 — Obligatorio para considerar completo el MVP

- CRUD de tareas y eventos.
- Estados de tarea.
- Categorías.
- Agenda y calendario.
- Búsqueda y filtros incluidos.
- Recordatorios: crear, reemplazar y cancelar.
- Parser de expresiones documentadas.
- Confirmación editable y gestión de ambigüedad.
- Persistencia offline.
- Corpus, métricas y pruebas críticas.

### P1 — Importante si el calendario académico lo permite

- Orden seleccionado por el usuario.
- Apertura del detalle desde la notificación.
- Recuperación reforzada después de reinicio.
- Mayor pulido visual y accesibilidad.
- Conservación visible de la frase original durante la confirmación.

### P2 — Posterior al proyecto

- Recurrencia.
- Respaldo local.
- Resumen semanal.
- Integraciones y sincronización.

## 20. Riesgos y mitigaciones

| Riesgo | Impacto | Mitigación |
|---|---|---|
| El parser crece hasta intentar comprender lenguaje libre. | Alto | Congelar una gramática explícita, versionar el corpus y tratar lo demás como no soportado. |
| Alarmas o permisos se comportan distinto entre versiones/dispositivos. | Alto | Encapsular el programador, probar en emulador y dispositivo real, y documentar limitaciones. |
| Las decisiones de fecha relativa cambian entre pruebas. | Alto | Inyectar un reloj y zona horaria; nunca usar la hora real directamente en pruebas. |
| Se intenta construir demasiada interfaz antes del núcleo. | Medio | Completar CRUD y pruebas de dominio antes del pulido visual. |
| El calendario consume demasiado tiempo. | Medio | Limitarlo a mes, selección de día e indicadores; no crear gestos o vistas semanales complejas. |
| Categorías o palabras clave entran en conflicto. | Medio | Detectar múltiples coincidencias y solicitar revisión; no resolver silenciosamente. |
| Una edición deja recordatorios duplicados. | Alto | Usar identificadores estables, operación reemplazar y pruebas de integración específicas. |
| La demo depende de una configuración manual. | Medio | Crear datos de demo y un guion reproducible antes de la fase final. |

## 21. Definición de terminado del MVP

El MVP está terminado cuando:

1. Todos los requisitos P0 están implementados.
2. Todas las historias críticas cumplen sus criterios de aceptación.
3. Las pruebas automatizadas acordadas pasan en una instalación limpia.
4. Se ejecutó el corpus completo y se generó el reporte de métricas.
5. Se verificó en al menos un emulador y un dispositivo físico, si está disponible.
6. No quedan defectos conocidos que bloqueen creación, consulta, edición, eliminación, parser o recordatorios.
7. Existe una versión instalable y un procedimiento documentado para compilarla.
8. El equipo dispone de datos de demostración y un guion de presentación.
9. Las limitaciones del parser y del prototipo están documentadas.

## 22. Guion sugerido para la demostración final

1. Abrir la agenda con datos locales y mostrar funcionamiento sin conexión.
2. Crear manualmente una tarea con categoría, prioridad y recordatorio.
3. Escribir “reunión el viernes a las 10 prioridad alta”.
4. Mostrar los campos extraídos y corregir uno antes de confirmar.
5. Ver la actividad en agenda y calendario.
6. Editar su horario y explicar la reprogramación del recordatorio.
7. Completar una tarea y filtrar pendientes/completadas.
8. Eliminar un evento y demostrar que desaparece de ambas vistas.
9. Presentar el corpus, las métricas y un caso ambiguo/no soportado.

## 23. Decisiones pendientes antes del desarrollo

Estas definiciones no deben bloquear el diseño inicial, pero conviene cerrarlas en la Fase 0:

1. Versión mínima de Android exigida o permitida por la facultad.
2. Formato exacto de fechas absolutas con año omitido.
3. Lista definitiva de alias para inferir evento y prioridad.
4. Si el recordatorio se expresará solo como anticipación o también como fecha/hora independiente.
5. Librería o componente visual del calendario, si se permite usar dependencias externas.
6. Dispositivos/emuladores que formarán la matriz mínima de pruebas.
7. Umbral final de precisión del parser si la cátedra establece uno distinto del 80 % propuesto.
8. Entregables académicos adicionales: memoria, diagramas UML, video, APK/AAB y repositorio.

## 24. Próximos pasos recomendados

1. Validar este PRD con todos los integrantes y el docente/tutor.
2. Resolver las decisiones pendientes de la sección 23.
3. Crear wireframes de baja fidelidad para los cinco flujos críticos.
4. Transformar los requisitos P0 en épicas e historias del tablero.
5. Definir el modelo de datos y los contratos del parser antes de implementar UI detallada.
6. Construir el primer corpus junto con las reglas, no al final del proyecto.
7. Planificar una primera entrega vertical: crear una tarea, persistirla y verla en la agenda.

