# OrganizaMe

OrganizaMe es una aplicación Android de organización personal para registrar tareas y eventos, asignarles fecha, horario, prioridad y categoría, visualizarlos en una agenda/calendario y recibir recordatorios locales.

Su componente diferencial es un parser determinístico de instrucciones simples en español. Por ejemplo, una frase como `estudiar mañana a las 18 descripción repasar unidad cinco recordatorio 30 minutos antes` se convierte en una actividad estructurada que el usuario puede revisar antes de guardar.

## Estado

El núcleo local del MVP ya incluye:

- CRUD de tareas, eventos y categorías con Room.
- Inicio, vista de hoy, listado por secciones y calendario.
- Búsqueda sin distinguir mayúsculas ni tildes y filtros combinados.
- Orden configurable por fecha/hora, prioridad, título o creación reciente.
- Recordatorios locales con notificaciones y reprogramación al reiniciar.
- Selección visual de fecha y hora mediante calendario y reloj.
- Tareas recurrentes diarias, semanales o mensuales.
- Resumen semanal navegable con pendientes, completadas, eventos y vencidas.
- Exportación e importación de un respaldo local en formato JSON.
- Parser determinístico con fecha, hora, tipo, prioridad, categoría, descripción y recordatorio.
- Confirmación editable antes de guardar.
- Corpus reproducible de 30 frases, pruebas unitarias e integración instrumentada con Room.

La aplicación se encuentra en etapa de integración, pruebas de comportamiento y preparación de la entrega.

## Funciones ampliadas

### Tareas recurrentes

Una tarea puede configurarse para repetirse todos los días, semanas o meses. Al completarla, OrganizaMe conserva la ocurrencia terminada y crea la siguiente con la misma información. Si la fecha original quedó atrasada, calcula la próxima ocurrencia futura y vuelve a programar su recordatorio cuando corresponda.

### Resumen semanal

Desde el inicio se puede consultar la semana actual, anterior o siguiente. La pantalla muestra contadores y agrupa las actividades por día.

### Respaldo local

La pantalla `Respaldo` permite exportar categorías y actividades a un archivo JSON. Al importar, se muestra una confirmación antes de reemplazar los datos locales y luego se reprograman los recordatorios futuros.

### Orden y planificación

El listado permite ordenar por fecha y hora, prioridad, título o fecha de creación. En el formulario, la fecha y la hora se seleccionan con controles nativos para evitar errores de formato.

## Tecnologías

- Kotlin
- Android nativo con layouts XML
- View Binding
- MVVM con `ViewModel`, `StateFlow` y efectos de una sola emisión
- Clean Architecture liviana con capas `ui`, `domain` y `data`
- Room sobre SQLite
- AlarmManager para los recordatorios locales
- Storage Access Framework para exportar e importar respaldos
- Pruebas unitarias e instrumentadas

## Arquitectura

La aplicación usa MVVM sobre una separación por capas:

- `ui`: Activities, estados de pantalla, efectos, ViewModels y sus factories.
- `domain`: interfaces de repositorio y casos de uso para actividades, categorías, parser, recordatorios y respaldos.
- `data`: implementaciones de repositorios con Room y adaptadores de servicios Android.
- `modelo`, `parser`, `filtros`, `resumen` y `respaldo`: entidades y lógica pura reutilizada por los casos de uso.

Las Activities ya no consultan Room directamente. Observan `StateFlow` respetando el ciclo de vida, delegan las operaciones a sus ViewModels y reciben eventos puntuales mediante `SharedFlow`. Las dependencias se construyen en factories pequeñas, sin incorporar todavía un framework de inyección.

La organización principal de paquetes es:

```text
com.santi.organizame
├── ui
│   ├── inicio, formulario, lista, hoy y calendario
│   ├── categorias, resumen, respaldo, parser y alarma
│   └── ViewModels, Activities y adapters de cada funcionalidad
├── domain
│   ├── repository
│   ├── recordatorios
│   └── usecase
├── data
│   ├── repository
│   ├── respaldo
│   └── recordatorios
└── modelo, parser, filtros, resumen y respaldo
```

## Gramática rápida del parser

- Fechas: `hoy`, `mañana`, `pasado mañana`, días de la semana o `dd/MM/aaaa`.
- Horas: `a las 18` o `18:30`.
- Prioridad: `prioridad alta`, `prioridad media`, `prioridad baja` o `urgente`.
- Tipo: `tarea`, `evento`, `reunión`, `cita` o `turno`.
- Descripción: `descripción ...`.
- Recordatorio: `a la hora`, `10 minutos antes`, `30 minutos antes`, `1 hora antes` o `1 día antes`.

Los recordatorios necesitan una fecha y una hora futuras. La pantalla de confirmación permite completar o corregir cualquier campo antes del guardado.

## Compilación y pruebas

Desde la raíz del proyecto:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug lintDebug
.\gradlew.bat assembleDebugAndroidTest
```

Con un emulador o dispositivo conectado, las pruebas de Room y del flujo completo se ejecutan con:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

La especificación completa se encuentra en [`docs/PRD_OrganizaMe.md`](docs/PRD_OrganizaMe.md).

