# Database Exporter Module

Este módulo se encarga de inspeccionar una base de datos relacional y generar el pseudo-formato (OpenApiSpec) que el módulo `core` puede transformar en una especificación OpenAPI.

## Características

*   **Inspección JDBC**: Utiliza metadatos de JDBC para leer tablas, columnas, tipos de datos, restricciones de nulidad y longitudes.
*   **Mapeo de Tipos**: Traduce tipos SQL estándar (VARCHAR, INTEGER, etc.) a tipos compatibles con OpenAPI (string, integer, number, boolean).
*   **Integración con Core**: Produce objetos `OpenApiSpec` listos para ser procesados por `OpenApiGenerator`.

## Uso

El componente principal es `DatabaseInspector`. Puedes pasarle una conexión JDBC y una lista de tablas para exportar:

```java
DatabaseInspector inspector = new DatabaseInspector();
OpenApiSpec spec = inspector.exportFromTables(connection, List.of("USERS", "PRODUCTS"));

// La generación de la especificación queda a cargo del módulo Core:
// OpenApiGenerator generator = new OpenApiGenerator();
// String yaml = generator.generate(spec);
```

## Dependencias

*   `openapi-generator-core`: Para los modelos y el motor de generación.
*   `Hibernate`: Para futuras funcionalidades de ingeniería inversa más avanzadas (incluido como dependencia base).
*   `H2`: Utilizado para pruebas unitarias e integración en memoria.
