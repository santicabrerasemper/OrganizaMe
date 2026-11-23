# OrganizaMe

OrganizaMe es una aplicación Android de organización personal para registrar tareas y eventos, asignarles fecha, horario, prioridad y categoría, visualizarlos en una agenda/calendario y recibir recordatorios locales.

Su componente diferencial es un parser determinístico de instrucciones simples en español. Por ejemplo, una frase como `estudiar mañana a las 18 descripción repasar unidad cinco recordatorio 30 minutos antes` se convierte en una actividad estructurada que el usuario puede revisar antes de guardar.

## Estado

El núcleo local del MVP ya incluye:

- CRUD de tareas, eventos y categorías con Room.
- Inicio, vista de hoy, listado por secciones y calendario.
- Búsqueda sin distinguir mayúsculas ni tildes y filtros combinados.
- Recordatorios locales con notificaciones y reprogramación al reiniciar.
- Parser determinístico con fecha, hora, tipo, prioridad, categoría, descripción y recordatorio.
- Confirmación editable antes de guardar.
- Corpus reproducible de 30 frases, pruebas unitarias e integración instrumentada con Room.

La aplicación se encuentra en etapa de integración, pruebas de comportamiento y preparación de la entrega.

## Tecnologías

- Kotlin
- Android nativo con layouts XML
- View Binding
- Separación por capas entre pantallas, parser/filtros, repositorios y persistencia
- Room sobre SQLite
- AlarmManager para los recordatorios locales
- Pruebas unitarias e instrumentadas

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

