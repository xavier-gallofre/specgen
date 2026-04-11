# OpenAPI Generator (Multi-project)

Proyecto base para la generación de especificaciones OpenAPI mediante un pseudo-formato intermedio.

## Estructura del Proyecto

*   **`openapi-generator-core`**: El motor principal (Librería). Contiene la lógica de transformación y plantillas FreeMarker.
*   **`openapi-generator-database-exporter`**: Módulo para inspeccionar bases de datos y generar el pseudo-formato a partir de tablas existentes.
*   *Próximos proyectos*: CLI (Micronaut), etc.

## Objetivo

Automatizar la creación de contratos de API mediante reglas y plantillas, permitiendo que la definición de modelos de negocio sea el punto central de la especificación.

## Características

*   **Core Reutilizable**: Diseñado como una librería para ser integrada en CLI (PicoCLI/Micronaut) o herramientas de ingeniería inversa (DB to OpenAPI).
*   **Motor de Plantillas**: Basado en **FreeMarker** para una flexibilidad total en la salida generada.
*   **Soporte Java 26**: Aprovecha las últimas características del lenguaje como `Records` y `Pattern Matching`.

## Ejecución de Tests

Para ejecutar todos los tests desde la raíz:
```bash
./gradlew test
```

## Flujo de Trabajo

Para comprender detalladamente cómo se generan las especificaciones paso a paso (parciales, intermedios y ensamblado final), consulta el documento de [Flujo de Generación de OpenAPI](README-WORKFLOW.md).

Para más detalles sobre la estructura interna del core, consulta [Architecture](openapi-generator-core/docs/architecture.md).
