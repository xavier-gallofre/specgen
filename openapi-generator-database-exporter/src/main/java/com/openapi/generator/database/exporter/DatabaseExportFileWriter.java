package com.openapi.generator.database.exporter;

import com.openapi.generator.core.OpenApiGenerator;
import com.openapi.generator.core.model.OpenApiSpec;
import com.openapi.generator.core.utils.FileUtils;
import com.openapi.generator.core.utils.YamlSerializer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Clase auxiliar para exportar metadatos de base de datos directamente a ficheros.
 */
public class DatabaseExportFileWriter {

    private final DatabaseInspector inspector;
    private final SqlInspector sqlInspector;
    private final YamlSerializer yamlSerializer;
    private final OpenApiGenerator generator;

    public DatabaseExportFileWriter() {
        this(new java.util.HashMap<>());
    }

    /**
     * Constructor que permite pasar propiedades adicionales para el generador OpenAPI.
     */
    public DatabaseExportFileWriter(Map<String, Object> additionalProperties) {
        this.inspector = new DatabaseInspector();
        this.sqlInspector = new SqlInspector();
        this.yamlSerializer = new YamlSerializer();
        this.generator = new OpenApiGenerator(additionalProperties);
    }

    /**
     * Exporta las tablas desde un DDL SQL y genera la especificación OpenAPI final en un fichero YAML.
     */
    public void exportSqlToYamlFile(String ddl, List<String> tableNames, String outputPath) throws IOException {
        try {
            OpenApiSpec spec = sqlInspector.exportFromSql(ddl, tableNames);
            String openApiContent = generator.generate(spec);
            FileUtils.writeToFile(outputPath, openApiContent);
        } catch (Exception e) {
            throw new IOException("Error al generar la especificación OpenAPI desde SQL", e);
        }
    }

    /**
     * Exporta las tablas desde un DDL SQL y guarda el formato intermedio en un fichero.
     */
    public void exportSqlToIntermediateFile(String ddl, List<String> tableNames, String outputPath) throws IOException {
        OpenApiSpec spec = sqlInspector.exportFromSql(ddl, tableNames);
        String intermediateContent = yamlSerializer.serialize(spec);
        FileUtils.writeToFile(outputPath, intermediateContent);
    }

    /**
     * Exporta las tablas y genera la especificación OpenAPI final en un fichero YAML.
     */
    public void exportToYamlFile(Map<String, Object> settings, List<String> tableNames, String outputPath) throws IOException {
        try {
            OpenApiSpec spec = inspector.exportFromTables(settings, tableNames);
            String openApiContent = generator.generate(spec);
            FileUtils.writeToFile(outputPath, openApiContent);
        } catch (Exception e) {
            throw new IOException("Error al generar la especificación OpenAPI", e);
        }
    }

    /**
     * Exporta las tablas y guarda el formato intermedio (OpenApiSpec) en un fichero de texto
     * (representación YAML del modelo intermedio).
     */
    public void exportToIntermediateFile(Map<String, Object> settings, List<String> tableNames, String outputPath) throws IOException {
        OpenApiSpec spec = inspector.exportFromTables(settings, tableNames);
        String intermediateContent = yamlSerializer.serialize(spec);
        FileUtils.writeToFile(outputPath, intermediateContent);
    }
}
