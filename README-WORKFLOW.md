# Flujo de Generación de OpenAPI

Este documento describe las fases del proceso de generación de especificaciones OpenAPI a partir de fuentes de datos (Base de Datos o SQL DDL), detallando los métodos involucrados en cada etapa.

## Fases del Proceso

El flujo de trabajo se divide en cinco fases principales, donde cada una depende de los resultados de la anterior.

### 1. Generación/Extracción de Intermedios Parciales
En esta fase se inspecciona la fuente de datos y se generan archivos de texto (`.txt`) que contienen la representación YAML del modelo intermedio (`ModelDefinition`) para cada tabla.

*   **Fuentes de datos:** Base de Datos (via Hibernate) o Script SQL DDL.
*   **Métodos principales:**
    *   `DatabaseExportFileWriter.exportToIntermediatePartialFiles(settings, tableNames, baseOutputDir)`: Para inspección de BD real.
    *   `SqlInspector.exportFromSql(ddl, tableNames)`: Para procesar DDL y obtener el `OpenApiSpec`.
*   **Resultado:** Archivos en `intermediate/partials/{TABLE_NAME}.txt`.

### 2. Combinación de Parciales en un Intermedio Completo
Se agrupan todos los archivos individuales de la fase anterior en un único archivo intermedio que representa la especificación completa del proyecto.

*   **Métodos principales:**
    *   `DatabaseExportFileWriter.mergePartials(baseOutputDir, finalIntermediateName, finalOpenApiName)`: Lee la carpeta `intermediate/partials`, deserializa los modelos y genera el archivo consolidado.
*   **Resultado:** Archivo en `intermediate/{finalIntermediateName}`.

### 3. Generación de OpenAPI Parcial a partir de Intermedio Parcial
A partir de un modelo intermedio (ya sea cargado desde memoria o desde un parcial `.txt`), se generan fragmentos de la especificación OpenAPI. Esta fase se divide en dos subfases para mayor granularidad.

*   **Subfase 3a (Paths):** Generación de los endpoints CRUD.
*   **Subfase 3b (Schemas):** Generación de los modelos de datos (View y Form).
*   **Métodos principales:**
    *   `OpenApiGenerator.generateFragment(spec, modelName, templateName)`: Método base para generar cualquier fragmento.
    *   `DatabaseExportFileWriter.exportToPartialFiles(...)`: Orquestador que utiliza el generador para crear los archivos `_paths.yaml` y `_schemas.yaml`.
*   **Resultado:** Archivos `_paths.yaml` y `_schemas.yaml` en `openapi/partials/`.

### 4. Combinación de Fragmentos en un Parcial de OpenAPI
Se combinan los fragmentos de rutas y esquemas de una misma tabla en un único archivo YAML parcial que representa la tabla completa en formato OpenAPI.

*   **Métodos principales:**
    *   `DatabaseExportFileWriter.exportToPartialFiles(...)`: Realiza la concatenación interna de los fragmentos generados en la fase 3.
*   **Resultado:** Archivos `{TABLE_NAME}.yaml` en `openapi/partials/`.

### 5. Combinación de Parciales en OpenAPI Completo
Finalmente, se integran todos los parciales de las tablas en una especificación OpenAPI completa y válida (incluyendo cabeceras, info, etc.).

*   **Métodos principales:**
    *   `OpenApiGenerator.generate(fullSpec)`: Genera el YAML completo a partir de un objeto `OpenApiSpec` consolidado.
    *   `DatabaseExportFileWriter.mergePartials(...)`: Coordina la lectura de todos los modelos y la generación del archivo final.
*   **Resultado:** Archivo en `openapi/{finalOpenApiName}`.

---

## Resumen de Responsabilidades por Módulo

### core
*   **`OpenApiGenerator`**: Motor de plantillas (FreeMarker) para transformar modelos intermedios en fragmentos o especificaciones completas.
*   **`YamlSerializer`**: Serialización/Deserialización de los modelos intermedios (`ModelDefinition` y `OpenApiSpec`).
*   **`TextUtils`**: Utilidades para formateo de nombres (CamelCase, SnakeCase, Pluralización).

### database-exporter
*   **`DatabaseInspector`**: Extracción de metadatos desde BD usando Hibernate.
*   **`SqlInspector`**: Extracción de metadatos desde scripts SQL DDL.
*   **`DatabaseExportFileWriter`**: Fachada de alto nivel que gestiona la persistencia en archivos y orquesta el flujo de parciales/merge.
