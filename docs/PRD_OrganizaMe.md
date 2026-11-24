# PRD — OrganizaMe

**Producto:** OrganizaMe  
**Versión del documento:** 1.2
**Estado:** MVP funcional validado / preparación de entrega
**Plataforma:** Android nativo  
**Tipo de entrega:** Prototipo académico / proyecto final  
**Fecha de actualización:** 24 de noviembre de 2026

---

## 1. Resumen ejecutivo

OrganizaMe es una aplicación Android de uso individual para registrar, consultar y administrar tareas y eventos personales desde un único lugar. Permite asignar fecha, hora, prioridad, categoría y recordatorio; consultar las actividades en una agenda diaria, un calendario mensual o un resumen semanal; configurar tareas recurrentes; y recibir notificaciones locales.

El diferencial del producto es una carga rápida mediante texto en español. Un parser determinístico, basado en reglas y expresiones regulares, transforma instrucciones acotadas —por ejemplo, “estudiar mañana a las 18 prioridad alta”— en campos estructurados. La aplicación nunca guarda automáticamente lo interpretado: antes de crear la actividad muestra una pantalla de confirmación con todos los campos editables y advierte cuando un dato es ambiguo o no pudo interpretarse.

El MVP es local, funciona sin conexión, no requiere cuenta e incluye persistencia con Room, recordatorios con AlarmManager, respaldo JSON mediante Storage Access Framework, pruebas automatizadas y evaluación cuantitativa reproducible del parser.

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
7. Demostrar separación de responsabilidades entre Activities, lógica de dominio, parser, repositorios, persistencia y programación de recordatorios.

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

### 3.4 Estado validado al 24/11/2026

| Evidencia | Resultado |
|---|---:|
| Pruebas unitarias | 46 aprobadas |
| Pruebas instrumentadas | 5 aprobadas |
| Errores de Android Lint | 0 |
| Corpus del parser | 30 frases |
| Exactitud por campo y coincidencia exacta del corpus soportado | 100 % |
| Recorrido end-to-end en Pixel 9a AVD / Android API 36 | Aprobado |

El recorrido end-to-end verificó alta manual y por parser, edición, validaciones, agenda de hoy, calendario, búsqueda sin tildes, filtros combinados, ordenamiento, categorías y palabras clave, notificación real, recurrencia, resumen semanal, exportación e importación JSON y restauración del estado inicial. Durante la prueba se detectó y corrigió el rótulo interno `Verde OrganizaMe`; la interfaz utiliza ahora el nombre neutro `Verde`.

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
- Orden configurable por fecha/hora, prioridad, título o creación reciente.
- Selección de fecha y hora mediante controles nativos de Android.
- Tareas recurrentes diarias, semanales o mensuales.
- Resumen semanal navegable con pendientes, completadas, eventos y vencidas.
- Exportación e importación de respaldos locales en formato JSON.

### 4.2 Fuera de alcance

- Asistente conversacional o comprensión irrestricta del lenguaje.
- Modelos generativos o dependencia obligatoria de APIs externas.
- Voz, dictado o procesamiento de audio.
- Sincronización entre dispositivos.
- Cuentas, autenticación, perfiles múltiples o colaboración.
- Integración con Google Calendar, Outlook u otros calendarios.
- Aplicación web, versión iOS o widgets.
- Ubicación, contactos o archivos adjuntos.
- Reglas avanzadas de recurrencia —días hábiles, excepciones o patrones personalizados—.
- Respaldo en la nube.
- Validación comercial o de mercado.

### 4.3 Extensiones posteriores al MVP

- Fecha y hora de finalización para eventos.
- Preferencias avanzadas de notificación.
- Tema oscuro y opciones adicionales de accesibilidad.
- Widgets de inicio y acciones rápidas del sistema.
- Sincronización opcional entre dispositivos o respaldo en la nube.

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

### 6.8 Recurrencia

- Solo las tareas pueden configurarse como recurrentes.
- Las frecuencias disponibles son diaria, semanal y mensual.
- Una tarea recurrente requiere una fecha inicial.
- Al completarla, se conserva la ocurrencia finalizada y se crea una única ocurrencia futura con la misma información.
- Si la fecha original quedó atrasada, la aplicación avanza tantas recurrencias como sean necesarias hasta obtener una fecha futura.
- Reabrir y volver a completar una ocurrencia ya procesada no debe generar duplicados.
- La nueva ocurrencia vuelve a programar su recordatorio únicamente si el instante calculado es futuro.

### 6.9 Respaldo local

- La exportación genera un archivo JSON versionado con todas las categorías y actividades.
- El usuario elige el destino mediante Storage Access Framework; la aplicación no requiere permisos amplios de almacenamiento.
- Antes de importar se valida la estructura y se informa cuántas actividades y categorías reemplazarán a los datos actuales.
- La importación requiere confirmación explícita, reemplaza los datos dentro de una transacción y reprograma los recordatorios futuros.
- Cancelar la importación no modifica los datos y deja un estado visible coherente.

## 7. Experiencia y navegación

### 7.1 Navegación principal

La aplicación parte de un **Inicio** que concentra los accesos principales:

1. **Hoy:** resumen del día, vencidas, pendientes, completadas y eventos.
2. **Tareas y eventos:** listado por secciones, búsqueda y filtros combinados.
3. **Calendario:** mes visible y actividades del día seleccionado.
4. **Nueva actividad:** formulario manual o confirmación de una frase interpretada.
5. **Categorías:** administración de nombres, colores y palabras clave.
6. **Ayuda:** ejemplos, gramática soportada y limitaciones del parser.
7. **Resumen semanal:** navegación por semana, contadores y actividades agrupadas por día.
8. **Respaldo:** exportación e importación de una copia local en JSON.

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
- Resumen semanal.
- Exportación e importación de respaldo.

### 7.3 Estados de interfaz obligatorios

Cada listado debe contemplar:

- Cargando, cuando corresponda.
- Con resultados.
- Sin actividades.
- Sin coincidencias para búsqueda/filtros.
- Error recuperable.

Los formularios deben indicar el campo inválido y la acción necesaria para corregirlo.

### 7.4 Identidad visual y branding

OrganizaMe debe transmitir calma, claridad y confianza. La interfaz utiliza espacios amplios, jerarquías tipográficas simples, tarjetas redondeadas y una paleta clara con verde como color principal y azul como acento de acción.

#### Paleta principal

| Token | Hexadecimal | Uso |
|---|---|---|
| Verde principal | `#0F766E` | Acciones principales, marca e indicadores activos. |
| Verde oscuro | `#134E4A` | Títulos, énfasis y barra de estado. |
| Azul secundario | `#2563EB` | Acciones alternativas, eventos y enlaces. |
| Fondo | `#F8FAFC` | Fondo general de todas las pantallas. |
| Texto principal | `#1E293B` | Texto de alto contraste. |

#### Colores derivados y estados

| Token | Hexadecimal | Uso |
|---|---|---|
| Superficie | `#FFFFFF` | Tarjetas, paneles y campos. |
| Contenedor verde | `#CCFBF1` | Acciones tonales y destacados suaves. |
| Contenedor azul | `#DBEAFE` | Eventos e información secundaria. |
| Borde | `#CBD5E1` | Delimitación de tarjetas y campos. |
| Texto secundario | `#475569` | Ayudas y metadatos. |
| Éxito | `#15803D` / `#DCFCE7` | Tareas completadas. |
| Advertencia | `#A16207` / `#FEF3C7` | Revisiones del parser. |
| Error | `#B91C1C` / `#FEE2E2` | Actividades vencidas y acciones destructivas. |

Los colores disponibles para categorías son verde (`#0F766E`), azul (`#2563EB`), verde oscuro (`#134E4A`), ámbar (`#A16207`) y violeta (`#7C3AED`). Los estados nunca deben comunicarse únicamente mediante color: también deben incluir texto, icono o etiqueta.

#### Reglas de componentes

- Botones y campos tienen una altura táctil mínima de 48 dp.
- Tarjetas y paneles usan radios de entre 14 y 18 dp, fondo blanco y bordes suaves.
- Los botones principales usan verde; el azul se reserva para acciones alternativas y eventos.
- Las acciones destructivas se presentan en rojo y requieren confirmación.
- Los títulos de pantalla usan verde oscuro; los textos extensos utilizan el color secundario de texto.
- El espaciado se construye con múltiplos de 4 dp y prioriza bloques de 12, 16, 20 y 24 dp.
- La interfaz inicial se entrega con tema claro; una variante oscura queda fuera del alcance del MVP.

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

### 8.6 Completar una tarea recurrente

1. El usuario completa una tarea con repetición configurada.
2. La aplicación marca la ocurrencia actual como completada y cancela su recordatorio.
3. Calcula la próxima fecha válida futura.
4. Crea una nueva ocurrencia pendiente con los mismos datos y la nueva fecha.
5. Programa su recordatorio futuro, si corresponde.
6. Actualiza listados, calendario y resumen semanal sin duplicar ocurrencias.

### 8.7 Consultar el resumen semanal

1. El usuario abre “Resumen semanal”.
2. La aplicación muestra el rango lunes-domingo, contadores y actividades por día.
3. El usuario puede navegar a la semana anterior, actual o siguiente.
4. Una semana vacía muestra un estado específico y contadores en cero.

### 8.8 Exportar e importar un respaldo

1. El usuario exporta un respaldo y elige el destino del archivo JSON.
2. La aplicación informa las cantidades exportadas.
3. Para recuperar datos, el usuario selecciona un archivo compatible.
4. La aplicación valida el archivo y muestra las cantidades que reemplazarán los datos actuales.
5. Al confirmar, reemplaza los datos en una transacción y reprograma recordatorios futuros.
6. Al cancelar, no modifica los datos.

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
| RF-A09 | Configurar una tarea con recurrencia diaria, semanal o mensual. | P0 |
| RF-A10 | Crear una única ocurrencia futura al completar una tarea recurrente. | P0 |

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
| RF-V08 | Permitir ordenar por fecha/hora, prioridad, título o creación reciente. | P0 |
| RF-V09 | Los filtros solo afectan la visualización y no modifican datos. | P0 |
| RF-V10 | Mostrar un resumen semanal navegable con pendientes, completadas, eventos y vencidas. | P0 |

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

### 9.6 Respaldo local

| ID | Requisito | Prioridad |
|---|---|---|
| RF-B01 | Exportar categorías y actividades a un archivo JSON versionado. | P0 |
| RF-B02 | Permitir que el usuario elija destino y origen mediante Storage Access Framework. | P0 |
| RF-B03 | Validar el archivo y mostrar cantidades antes de importar. | P0 |
| RF-B04 | Solicitar confirmación antes de reemplazar los datos locales. | P0 |
| RF-B05 | Reemplazar los datos en una transacción y reprogramar recordatorios futuros. | P0 |
| RF-B06 | Cancelar una importación sin modificar la información actual. | P0 |

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

### HU-08 — Repetir una tarea

**Como** usuario, **quiero** configurar una tarea recurrente **para** no volver a cargarla manualmente.

Criterios:

- La tarea permite seleccionar repetición diaria, semanal o mensual.
- Una recurrencia sin fecha inicial no puede guardarse.
- Al completar una ocurrencia se crea una única tarea pendiente con fecha futura.
- La ocurrencia completada se conserva y la nueva hereda sus datos.
- Reabrir y volver a completar la misma ocurrencia no genera duplicados.

### HU-09 — Consultar la semana

**Como** usuario, **quiero** ver un resumen semanal **para** entender rápidamente mi carga de actividades.

Criterios:

- Se muestra el rango lunes-domingo y los contadores de pendientes, completadas, eventos y vencidas.
- Las actividades se agrupan por día.
- Es posible navegar a la semana anterior, actual y siguiente.
- Una semana sin actividades muestra contadores en cero y un estado vacío.

### HU-10 — Respaldar y recuperar datos

**Como** usuario, **quiero** exportar e importar mis datos **para** conservar una copia local recuperable.

Criterios:

- La exportación crea un JSON válido con actividades y categorías.
- La aplicación informa las cantidades exportadas.
- Un archivo inválido no reemplaza los datos actuales.
- Antes de importar se muestran las cantidades y se solicita confirmación.
- Cancelar conserva los datos; confirmar reemplaza el contenido y reprograma recordatorios futuros.

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
| recurrence | NONE / DAILY / WEEKLY / MONTHLY | Solo para tareas; sin repetición por defecto. |
| recurrenceProcessed | Boolean | Evita crear dos veces la siguiente ocurrencia. |
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

### 13.1 Stack implementado

- Kotlin y Java 11.
- Android nativo con Activities, View Binding y layouts XML.
- Repositorios como límite entre UI, dominio y persistencia.
- Room 2.8.5 sobre SQLite, con migración versionada del esquema.
- AlarmManager y BroadcastReceiver para recordatorios y reprogramación tras reinicio o actualización.
- Storage Access Framework para crear y abrir respaldos sin permisos amplios de almacenamiento.
- API `java.time`, reglas determinísticas y expresiones regulares para el parser.
- JUnit 4 para pruebas unitarias y AndroidX Test/Room Testing para pruebas instrumentadas.

### 13.2 Separación mínima de componentes

```text
Activities / View Binding
        ↓
Repositorios ─────────────── Room
        ├─────────────────── Programador de recordatorios / receptores
        ├─────────────────── Respaldo JSON / Storage Access Framework
        └─────────────────── Dominio puro: parser, filtros, orden, recurrencia y resumen
```

El parser no debe depender de Android ni de la base de datos. Debe recibir fecha/hora base y categorías mediante parámetros para que sus pruebas sean determinísticas.

### 13.3 Compatibilidad

- `minSdk 26`.
- `targetSdk 36` y `compileSdk 36.1`.
- Versión de aplicación actual: `1.0` (`versionCode 1`).
- La matriz ejecutada incluye Pixel 9a AVD sobre Android API 36.

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
| RNF-12 | Todas las pantallas deben reutilizar los tokens y componentes definidos en la identidad visual, evitando colores de marca escritos directamente en los layouts o en la lógica. |

Los límites de rendimiento son objetivos de aceptación para el prototipo, no garantías para todos los dispositivos Android.

## 15. Evaluación del parser

### 15.1 Corpus

Se utiliza un conjunto versionado de 30 frases válidas soportadas, acompañado por pruebas unitarias separadas para casos ambiguos, inválidos y no soportados. El corpus registra:

- Frase y resultado esperado por campo.
- Fecha y hora base fija: 16/11/2026 15:00.
- Zona horaria: `America/Argentina/Buenos_Aires`.
- Categorías y palabras clave controladas.

Los casos unitarios adicionales documentan las advertencias esperadas y clasifican entradas ambiguas, inválidas o no soportadas.

### 15.2 Métricas

- Exactitud por campo = campos correctos / campos evaluados.
- Exactitud completa = frases con todos los campos correctos / frases válidas.
- Detección de ambigüedad = ambiguas marcadas / casos ambiguos.
- Rechazo controlado = casos inválidos que generan advertencia y no cierre inesperado / casos inválidos.

El informe final debe mostrar una matriz por caso y un resumen de errores frecuentes. Una frase parcialmente interpretada no cuenta como coincidencia completa, aunque sus campos acertados sí cuentan en la métrica por campo.

Resultado actual: 100 % en título, tipo, fecha, hora, prioridad, categoría, descripción, recordatorio y coincidencia exacta para las 30 frases del corpus soportado. Este valor no implica comprensión general del lenguaje natural; solo representa la gramática documentada.

## 16. Estrategia de pruebas

### 16.1 Pruebas unitarias

- Todas las reglas del parser con reloj fijo.
- Conversión de fecha relativa y día semanal.
- Validación de fechas y horas.
- Normalización de tildes y mayúsculas.
- Detección de categorías y conflictos de palabras clave.
- Reglas de tareas/eventos y recordatorios.
- Ordenamiento y combinación de filtros.
- Cálculo y generación de recurrencias.
- Cálculo de resumen semanal.
- Serialización y deserialización del respaldo.

### 16.2 Pruebas de integración

- DAO de Room: crear, consultar, editar y eliminar.
- Repositorio y casos de uso.
- Programar, reemplazar y cancelar recordatorios usando abstracciones verificables.
- Migración de base de datos, si la versión del esquema cambia durante el proyecto.
- Reemplazo transaccional de categorías y actividades desde un respaldo.

### 16.3 Pruebas de interfaz/aceptación

- Crear actividad manual.
- Crear desde frase y corregir datos.
- Editar actividad con recordatorio.
- Completar y reabrir tarea.
- Eliminar actividad.
- Buscar y combinar filtros.
- Navegar entre agenda y calendario.
- Denegar permiso de notificaciones sin impedir el guardado.
- Completar una tarea recurrente y comprobar la nueva ocurrencia.
- Navegar por el resumen semanal.
- Exportar, validar e importar un respaldo local.
- Comprobar una notificación real en el emulador.

### 16.4 Prueba exploratoria de usabilidad

Realizar una sesión breve con participantes disponibles, usando tareas guiadas. Registrar tiempo, errores, dudas y comentarios; no presentar el resultado como validación de mercado. Recorridos sugeridos:

1. Crear manualmente una tarea para mañana.
2. Crear un evento mediante una frase.
3. Corregir una interpretación ambigua.
4. Encontrar y completar una tarea.
5. Cambiar un recordatorio y eliminar otra actividad.

### 16.5 Resultado de la ejecución final

El 24/11/2026 se ejecutaron `testDebugUnitTest`, `connectedDebugAndroidTest`, `lintDebug` y `assembleDebug`. El resultado fue exitoso: 46 pruebas unitarias, 5 instrumentadas y cero errores de lint. Además se realizó un recorrido end-to-end sobre un Pixel 9a AVD que incluyó una notificación real, un respaldo JSON con tres actividades y cuatro categorías, la importación confirmada del respaldo inicial y la comprobación final de cero actividades y las tres categorías originales.

## 17. Datos de demostración

El entorno de demostración debe usar datos controlados, creados mediante el guion o recuperados desde un respaldo JSON separado de cualquier dato real. Conjunto sugerido:

- Dos tareas pendientes para hoy.
- Una tarea completada.
- Una tarea futura de prioridad alta con recordatorio.
- Un evento de día completo.
- Un evento con hora y recordatorio.
- Una actividad sin fecha.
- Tres categorías con palabras clave.

La aplicación no inserta datos de demo automáticamente. Antes de cada exposición se debe restaurar el respaldo acordado para evitar duplicados y asegurar un recorrido reproducible.

## 18. Roadmap de implementación

### Fase 0 — Alineación y diseño — Completada

- Confirmar requisitos de la facultad, versión mínima de Android y formato de entrega.
- Validar reglas de negocio y gramática soportada.
- Crear wireframes de las pantallas mínimas.
- Definir esquema de datos y contratos de dominio.
- Preparar repositorio, ramas, convenciones y CI si corresponde.

**Salida:** decisiones cerradas, backlog inicial y proyecto compilable.

### Fase 1 — Núcleo local — Completada

- Entidades, DAO, Room y repositorios.
- Casos de uso de tareas, eventos y categorías.
- Formulario manual y detalle.
- Agenda básica.
- Pruebas unitarias y de persistencia.

**Salida:** ciclo CRUD local funcional.

### Fase 2 — Consulta y calendario — Completada

- Agenda agrupada.
- Calendario mensual.
- Búsqueda, filtros y estados vacíos.
- Completar/reabrir tareas.

**Salida:** organización y consulta completas.

### Fase 3 — Recordatorios — Completada

- Permisos y canal de notificaciones.
- Programación local.
- Reprogramación y cancelación.
- Pruebas de reglas y recorrido manual en dispositivo.

**Salida:** recordatorios consistentes con el ciclo de vida de la actividad.

### Fase 4 — Parser — Completada

- Normalización y reglas temporales.
- Prioridad, categoría, tipo y título.
- Modelo de ambigüedad/advertencias.
- Pantalla de confirmación.
- Corpus automatizado y reporte inicial.

**Salida:** carga por texto medible y corregible.

### Fase 5 — Integración y entrega — En curso

- Completado: recurrencia, resumen semanal y respaldo local.
- Completado: pruebas automatizadas y end-to-end de los flujos críticos.
- Completado: corrección de defectos detectados y reporte del parser.
- En curso: datos controlados de demostración, revisión de accesibilidad y mensajes.
- Pendiente: documentación final, manual breve y ensayo de la presentación.

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
- Recurrencia diaria, semanal y mensual.
- Resumen semanal.
- Respaldo local JSON.
- Ordenamiento configurable.

### P1 — Importante si el calendario académico lo permite

- Apertura del detalle desde la notificación.
- Recuperación reforzada después de reinicio.
- Mayor pulido visual y accesibilidad.
- Conservación visible de la frase original durante la confirmación.

### P2 — Posterior al proyecto

- Tema oscuro y widgets.
- Reglas avanzadas de recurrencia.
- Integraciones y sincronización.
- Respaldo opcional en la nube.

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

Estado actual: el software cumple los flujos funcionales y las pruebas automatizadas/end-to-end definidas para el prototipo. Permanecen como tareas de cierre académico la validación en dispositivo físico —si la cátedra la exige—, la preparación del paquete de entrega y el ensayo final de la presentación.

## 22. Guion sugerido para la demostración final

1. Abrir la agenda con datos locales y mostrar funcionamiento sin conexión.
2. Crear manualmente una tarea con categoría, prioridad y recordatorio.
3. Escribir “reunión el viernes a las 10 prioridad alta”.
4. Mostrar los campos extraídos y corregir uno antes de confirmar.
5. Ver la actividad en agenda y calendario.
6. Editar su horario y explicar la reprogramación del recordatorio.
7. Completar una tarea y filtrar pendientes/completadas.
8. Completar una tarea recurrente y mostrar la nueva ocurrencia futura.
9. Mostrar el resumen semanal y navegar entre semanas.
10. Exportar un respaldo JSON y explicar la confirmación de importación.
11. Eliminar un evento y demostrar que desaparece de ambas vistas.
12. Presentar el corpus, las métricas y un caso ambiguo/no soportado.

## 23. Decisiones técnicas cerradas y pendientes académicos

### Decisiones cerradas

1. Compatibilidad: `minSdk 26`, `targetSdk 36` y `compileSdk 36.1`.
2. Fechas absolutas documentadas en formato `dd/MM/yyyy`; las fechas relativas se calculan con el reloj local.
3. Alias del parser acotados y documentados en la ayuda de la aplicación.
4. Recordatorios expresados como anticipaciones predefinidas respecto de la fecha y hora de la actividad.
5. Calendario mensual implementado con componentes Android propios, sin dependencia externa específica de calendario.
6. Matriz inicial de pruebas: Pixel 9a AVD sobre Android API 36.
7. Umbral del corpus: 80 % mínimo; resultado actual: 100 % para la gramática soportada.

### Pendientes académicos

1. Confirmar si la cátedra exige prueba adicional en un dispositivo físico.
2. Confirmar el formato final de entrega: APK o AAB, memoria, diagramas UML y video.
3. Incorporar el guion definitivo de exposición y responsables de cada parte.

## 24. Próximos pasos recomendados

1. Validar la versión 1.2 de este PRD con todos los integrantes y el docente/tutor.
2. Preparar un APK de entrega reproducible y registrar su versión.
3. Ejecutar la matriz final en el dispositivo o emulador exigido por la cátedra.
4. Incorporar capturas y videos del recorrido validado a la bitácora del foro.
5. Preparar la memoria técnica, diagramas y reporte del corpus.
6. Ensayar el guion de demostración con datos controlados y respaldo previo.
7. Congelar la versión candidata y corregir únicamente defectos que bloqueen la entrega.

