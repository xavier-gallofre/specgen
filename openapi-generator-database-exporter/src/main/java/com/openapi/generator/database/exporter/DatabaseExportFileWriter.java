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
    private final YamlSerializer yamlSerializer;
    private final OpenApiGenerator generator;

    public DatabaseExportFileWriter() {
        this.inspector = new DatabaseInspector();
        this.yamlSerializer = new YamlSerializer();
        this.generator = new OpenApiGenerator();
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
