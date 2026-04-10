# Database Exporter Module

Este módulo se encarga de inspeccionar una base de datos relacional y generar el pseudo-formato (OpenApiSpec) que el módulo `core` puede transformar en una especificación OpenAPI.

## Características

*   **Inspección Hibernate/JDBC**: Utiliza metadatos de JDBC a través de Hibernate para una extracción de metadatos robusta y multi-base de datos.
*   **Inspección SQL DDL**: Capacidad de generar modelos a partir de scripts SQL (`CREATE TABLE`) sin necesidad de una base de datos real.
*   **Mapeo de Tipos**: Traduce tipos SQL estándar (VARCHAR, INTEGER, etc.) a tipos compatibles con OpenAPI (string, integer, number, boolean).
*   **Exportación a Archivos**: Herramientas auxiliares para volcar resultados (YAML final e intermedio) directamente al sistema de archivos.

## Uso

### Inspección de Base de Datos Real
El componente principal es `DatabaseInspector`. Utiliza Hibernate para conectarse a cualquier base de datos configurada:

```java
DatabaseInspector inspector = new DatabaseInspector();
Map<String, Object> settings = Map.of(
    "hibernate.connection.url", "jdbc:h2:mem:test",
    "hibernate.connection.driver_class", "org.h2.Driver"
);
OpenApiSpec spec = inspector.exportFromTables(settings, List.of("USERS"));
```

### Inspección desde SQL DDL
Para generar la especificación sin conectarse a una base de datos externa, usa `SqlInspector`. Internamente procesa el DDL en una instancia de H2 efímera:

```java
SqlInspector sqlInspector = new SqlInspector();
String ddl = "CREATE TABLE CUSTOMERS (ID NUMBER(10) PRIMARY KEY, NAME VARCHAR2(50))";
OpenApiSpec spec = sqlInspector.exportFromSql(ddl, List.of("CUSTOMERS"));
```

### Exportación Directa a Archivos
`DatabaseExportFileWriter` combina la inspección y la generación de archivos:

```java
DatabaseExportFileWriter writer = new DatabaseExportFileWriter();

// Desde Base de Datos
writer.exportToYamlFile(settings, List.of("USERS"), "api.yaml");

// Desde SQL DDL
writer.exportSqlToYamlFile(ddl, List.of("CUSTOMERS"), "api_from_sql.yaml");
```

## Dependencias

*   `openapi-generator-core`: Para los modelos y el motor de generación.
*   `Hibernate`: Para futuras funcionalidades de ingeniería inversa más avanzadas (incluido como dependencia base).
*   `H2`: Utilizado para pruebas unitarias e integración en memoria.
