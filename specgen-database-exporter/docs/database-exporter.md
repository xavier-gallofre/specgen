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
`DatabaseExportFileWriter` combina la inspección y la generación de archivos. Soporta configuración adicional para personalizar la salida OpenAPI (vía propiedades) y la ubicación de las plantillas:

```java
// Propiedades para la base de datos (Hibernate)
Map<String, Object> dbSettings = Map.of(
    "hibernate.connection.url", "jdbc:h2:mem:test",
    "hibernate.connection.driver_class", "org.h2.Driver"
);

// Propiedades para la generación de OpenAPI y plantillas
Map<String, Object> apiProps = Map.of(
    "api.title", "Mi API Personalizada",
    "templates.path", "/ruta/a/mis/plantillas"
);

DatabaseExportFileWriter writer = new DatabaseExportFileWriter(apiProps);
writer.exportToYamlFile(dbSettings, List.of("USERS"), "api.yaml");
```

### Exportación por Parciales y Consolidación (Merge)
Es posible generar archivos individuales por cada tabla (parciales) y luego combinarlos en archivos finales. Esto es útil para flujos de trabajo donde se revisan los cambios tabla por tabla.

Cuando se generan parciales OpenAPI, `DatabaseExportFileWriter` genera tres tipos de archivos en `openapi/partials/`:
*   `TABLE.yaml`: El parcial completo de la tabla (incluye `paths` y `components/schemas`).
*   `TABLE_paths.yaml`: Solo los fragmentos de rutas (`paths`) asociados a la tabla.
*   `TABLE_schemas.yaml`: Solo los fragmentos de esquemas (`components/schemas`) asociados a la tabla.

Esto permite una mayor granularidad al trabajar con parciales de la especificación.

```java
DatabaseExportFileWriter writer = new DatabaseExportFileWriter(apiProps);
String outputDir = "generated";

// Genera parciales en generated/openapi/partials y generated/intermediate/partials
// Incluyendo los nuevos fragmentos _paths.yaml y _schemas.yaml
writer.exportToPartialFiles(dbSettings, List.of("USERS", "PRODUCTS"), outputDir);

// Combina los parciales en archivos finales
writer.mergePartials(outputDir, "full-intermediate.txt", "full-openapi.yaml");
```

## Configuración Avanzada

### Uso de Archivos .properties
Puedes utilizar `FileUtils.loadProperties` para cargar configuraciones externas tanto para la base de datos como para el generador:

```java
Properties dbProps = FileUtils.loadProperties("database.properties");
Map<String, Object> settings = (Map) dbProps;

Properties apiProps = FileUtils.loadProperties("api.properties");
Map<String, Object> additionalProps = (Map) apiProps;

DatabaseExportFileWriter writer = new DatabaseExportFileWriter(additionalProps);
writer.exportToYamlFile(settings, List.of("USERS"), "api.yaml");
```

### Personalización de Plantillas
Si se define la propiedad `templates.path`, el motor buscará `main.ftl` y sus fragmentos en esa ruta. En las plantillas, puedes acceder a las propiedades personalizadas usando `.vars`:

```ftl
info:
  title: ${.vars['api.title']!'API por Defecto'}
```

### CLI de Exportación (Showcase)
En el módulo `specgen-showcase`, se incluye una utilidad de línea de comandos (`CliExporterApp`) para realizar exportaciones rápidas:

```bash
# Exportar desde un archivo DDL SQL
java CliExporterApp --sql schema.sql [directorio_salida]

# Exportar desde una base de datos JDBC (pide credenciales interactivas)
java CliExporterApp --jdbc jdbc_url [directorio_salida]
```

Esta utilidad genera exclusivamente los **parciales intermedios** (archivos `.txt` en `intermediate/partials`), facilitando la integración en flujos de CI/CD donde la generación de la especificación OpenAPI final se delega a un paso posterior de consolidación (merge).

## Diccionarios de Nombres (Renombrado de Tablas y Columnas)

El exportador permite utilizar diccionarios en formato CSV para renombrar tablas y columnas. Esto es útil cuando los nombres en la base de datos no siguen las convenciones deseadas para la API.

### Configuración del Diccionario

Se utiliza la clase `NameDictionary` para cargar los archivos CSV:

```java
NameDictionary dictionary = new NameDictionary();
dictionary.loadTableDictionary("tablas.csv");
dictionary.loadColumnDictionary("columnas.csv");
dictionary.loadGeneralDictionary("terminos_generales.csv");

// Aplicar al inspector
DatabaseInspector inspector = new DatabaseInspector();
inspector.setDictionary(dictionary);

// O al SqlInspector
SqlInspector sqlInspector = new SqlInspector();
sqlInspector.setDictionary(dictionary);
```

## Adaptación de Plantillas por Tipo

El generador de esquemas (`schemas.ftl`) utiliza plantillas específicas para cada tipo de propiedad. Si no se encuentra una plantilla específica, se utiliza una genérica.

### Plantillas Disponibles
- `property_string.ftl`: Para propiedades de tipo `string`.
- `property_number.ftl`: Para propiedades de tipo `integer` y `number`.
- `property_boolean.ftl`: Para propiedades de tipo `boolean`.
- `property_date.ftl`: Para propiedades de tipo `date` (genera `format: date`).
- `property_generic.ftl`: Plantilla por defecto para cualquier otro tipo.

Esto permite personalizar la generación de YAML para cada tipo de dato de forma independiente, facilitando la inclusión de campos específicos como `maxLength`, `format`, o validaciones personalizadas.

### Mapeo de Tipos de Fecha
El exportador ahora mapea los tipos SQL `DATE`, `TIMESTAMP` y `TIME` al tipo intermedio `date`, lo que activa automáticamente el uso de la plantilla `property_date.ftl` en la generación de la especificación OpenAPI.

### Formato de los Archivos CSV

Los archivos deben tener el formato `nombre_original,nuevo_nombre` (uno por línea).

*   **Diccionario de Tablas**: Traduce nombres de tablas.
    *   Ejemplo: `T_USUARIOS,Usuario`
*   **Diccionario de Columnas**: Soporta nombres específicos (Tabla.Columna) y genéricos.
    *   Ejemplo: `T_USUARIOS.USR_ID,id` (específico)
    *   Ejemplo: `USR_NAME,nombre` (genérico para cualquier tabla)
*   **Diccionario General**: Términos que se aplican si no se encuentra en los anteriores.
    *   Ejemplo: `DESC,descripcion`

### Prioridad de Resolución

La prioridad al buscar un nombre es la siguiente:

1.  **Tablas**:
    1.  Diccionario de Tablas.
    2.  Diccionario General.
    3.  Nombre original (si no hay coincidencia).

2.  **Columnas**:
    1.  Columna específica (`Tabla.Columna`) en el Diccionario de Columnas.
    2.  Nombre de columna genérico en el Diccionario de Columnas.
    3.  Diccionario General.
    4.  Nombre original (si no hay coincidencia).

## Reglas de Exportación Personalizadas

Es posible aplicar reglas personalizadas durante la inspección de la base de datos para transformar tipos de datos o aplicar lógicas específicas.

### Interfaz `ExportRule`
Para implementar una regla, debes extender la interfaz `ExportRule`:

```java
public class MiReglaPersonalizada implements ExportRule {
    @Override
    public PropertyDefinition apply(String tableName, String columnName, String sqlType, Integer columnSize, PropertyDefinition currentProp) {
        // Lógica de transformación
        return nuevaPropiedad;
    }

    @Override
    public String getReviewNote(String tableName, String columnName, String sqlType, Integer columnSize) {
        // Nota para el informe de revisión (Markdown)
        return "Se ha aplicado un cambio que requiere revisión.";
    }
}
```

### Transformación VARCHAR(1) a Boolean
Se incluye de serie la regla `VarcharToBooleanRule`, que detecta columnas de tamaño 1 (VARCHAR, CHAR) y las marca como booleanos, asumiendo que contienen valores como 'S/N', '1/0', etc.

### Registro de Cambios (`revisar.md`)
Cuando se aplican reglas que modifican el tipo original, se genera automáticamente un archivo `revisar.md` en el directorio de salida. Este informe detalla:
*   La tabla y columna afectada.
*   La explicación del riesgo o motivo del cambio.
*   Un enlace directo al archivo parcial intermedio para facilitar la inspección.

### Configuración

```java
ExportRuleManager ruleManager = new ExportRuleManager();
ruleManager.addRule(new VarcharToBooleanRule());

DatabaseExportFileWriter writer = new DatabaseExportFileWriter();
writer.setRuleManager(ruleManager);

writer.exportToPartialFiles(settings, tables, "outputDir");
```

## Dependencias

*   `specgen-core`: Para los modelos y el motor de generación.
*   `Hibernate`: Para futuras funcionalidades de ingeniería inversa más avanzadas (incluido como dependencia base).
*   `H2`: Utilizado para pruebas unitarias e integración en memoria.
