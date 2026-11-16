# Evaluación reproducible del parser

## Objetivo

Medir el parser determinístico de OrganizaMe con un corpus versionado y una fecha base fija. La evaluación se ejecuta automáticamente y falla si la precisión de cualquier campo o la coincidencia exacta baja del 80 % definido para el MVP.

## Configuración

- Corpus: `app/src/test/resources/corpus_parser.csv`
- Cantidad de frases: 30
- Fecha y hora base: 16/11/2026 15:00
- Zona horaria: `America/Argentina/Buenos_Aires`
- Categorías controladas:
  - Personal: `casa`, `hogar`
  - Estudio: `facultad`, `universidad`
  - Trabajo: `oficina`, `laboral`

## Resultados actuales

| Métrica | Resultado |
|---|---:|
| Título | 100 % |
| Tipo | 100 % |
| Fecha | 100 % |
| Hora | 100 % |
| Prioridad | 100 % |
| Categoría | 100 % |
| Coincidencia exacta de la actividad | 100 % |

## Ejecución

Desde la raíz del proyecto:

```powershell
.\gradlew.bat testDebugUnitTest --tests "com.santi.organizame.parser.EvaluacionCorpusParserTest"
```

La prueba lee el corpus, compara cada campo esperado y verifica el umbral mínimo. También imprime las métricas en la salida de Gradle.

## Alcance del resultado

El corpus cubre únicamente la gramática documentada: fechas relativas y absolutas, días semanales, horarios de 24 horas, tipos explícitos y alias acotados, prioridades y categorías configuradas. El 100 % no representa comprensión de lenguaje natural general. Casos ambiguos, inválidos y expresiones no soportadas se prueban por separado en `ParserActividadTest`.
