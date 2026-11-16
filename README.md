# OrganizaMe

OrganizaMe es una aplicación Android de organización personal para registrar tareas y eventos, asignarles fecha, horario, prioridad y categoría, visualizarlos en una agenda/calendario y recibir recordatorios locales.

Su componente diferencial será un parser determinístico de instrucciones simples en español. Por ejemplo, una frase como `estudiar mañana a las 18` se convertirá en una actividad estructurada que el usuario deberá revisar antes de guardar.

## Estado

El núcleo local del MVP ya incluye:

- CRUD de tareas, eventos y categorías con Room.
- Agenda, calendario, búsqueda y filtros combinados.
- Recordatorios locales con notificaciones.
- Parser determinístico de frases en español con confirmación editable.
- Corpus reproducible de 30 frases y métricas automatizadas.

La aplicación se encuentra en etapa de integración, pruebas de comportamiento y preparación de la entrega.

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

