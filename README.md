# OrganizaMe

OrganizaMe es una aplicación Android de organización personal para registrar tareas y eventos, asignarles fecha, horario, prioridad y categoría, visualizarlos en una agenda/calendario y recibir recordatorios locales.

Su componente diferencial será un parser determinístico de instrucciones simples en español. Por ejemplo, una frase como `estudiar mañana a las 18` se convertirá en una actividad estructurada que el usuario deberá revisar antes de guardar.

## Estado

Proyecto base creado para iniciar el desarrollo del MVP.

## Tecnologías

- Kotlin
- Android nativo con layouts XML
- View Binding
- Arquitectura MVVM
- Room sobre SQLite
- WorkManager y AlarmManager
- Pruebas unitarias e instrumentadas

## Primeros incrementos

1. Modelar tareas, eventos y categorías.
2. Implementar persistencia local con Room.
3. Crear el flujo manual de alta de tareas.
4. Mostrar las actividades en una agenda.
5. Incorporar calendario y recordatorios.
6. Implementar y evaluar el parser en español.

La especificación completa se encuentra en [`docs/PRD_OrganizaMe.md`](docs/PRD_OrganizaMe.md).

