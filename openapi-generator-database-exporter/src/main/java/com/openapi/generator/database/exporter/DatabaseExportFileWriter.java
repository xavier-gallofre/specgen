package com.openapi.generator.database.exporter;

import com.openapi.generator.core.OpenApiGenerator;
import com.openapi.generator.core.model.ModelDefinition;
import com.openapi.generator.core.model.OpenApiSpec;
import com.openapi.generator.core.utils.FileUtils;
import com.openapi.generator.core.utils.YamlSerializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * Exporta las tablas y genera archivos parciales (OpenAPI e intermedio) por cada tabla.
     * Los parciales se guardan en subcarpetas del directorio base indicado.
     */
    public void exportToPartialFiles(Map<String, Object> settings, List<String> tableNames, String baseOutputDir) throws IOException {
        try {
            OpenApiSpec spec = inspector.exportFromTables(settings, tableNames);
            
            Path basePath = Path.of(baseOutputDir);
            Path intermediatePartialsPath = basePath.resolve("intermediate/partials");
            Path openapiPartialsPath = basePath.resolve("openapi/partials");

            for (ModelDefinition model : spec.models()) {
                // 1. Parcial Intermedio (YAML del ModelDefinition)
                String modelYaml = yamlSerializer.serialize(model);
                FileUtils.writeToFile(intermediatePartialsPath.resolve(model.name() + ".txt").toString(), modelYaml);

                // 2. Parcial OpenAPI (YAML parcial generado por template)
                String partialOpenApi = generator.generatePartial(spec, model.name());
                FileUtils.writeToFile(openapiPartialsPath.resolve(model.name() + ".yaml").toString(), partialOpenApi);
            }
        } catch (Exception e) {
            throw new IOException("Error al generar archivos parciales", e);
        }
    }

    /**
     * Exporta las tablas y genera archivos parciales intermedios (formato interno) por cada tabla.
     * Los parciales se guardan en la subcarpeta intermediate/partials del directorio base indicado.
     */
    public void exportToIntermediatePartialFiles(Map<String, Object> settings, List<String> tableNames, String baseOutputDir) throws IOException {
        try {
            OpenApiSpec spec = inspector.exportFromTables(settings, tableNames);
            Path intermediatePartialsPath = Path.of(baseOutputDir).resolve("intermediate/partials");

            for (ModelDefinition model : spec.models()) {
                String modelYaml = yamlSerializer.serialize(model);
                FileUtils.writeToFile(intermediatePartialsPath.resolve(model.name() + ".txt").toString(), modelYaml);
            }
        } catch (Exception e) {
            throw new IOException("Error al generar archivos parciales intermedios", e);
        }
    }

    /**
     * Combina los archivos parciales en archivos finales consolidados.
     * @param baseOutputDir Directorio base donde se encuentran las carpetas partials.
     * @param finalIntermediateName Nombre del archivo intermedio final (ej: "intermediate.txt").
     * @param finalOpenApiName Nombre del archivo OpenAPI final (ej: "openapi.yaml").
     */
    public void mergePartials(String baseOutputDir, String finalIntermediateName, String finalOpenApiName) throws IOException {
        try {
            Path basePath = Path.of(baseOutputDir);
            Path intermediatePartialsPath = basePath.resolve("intermediate/partials");
            Path openapiPartialsPath = basePath.resolve("openapi/partials");

            // 1. Merge Intermedio
            List<ModelDefinition> models = new ArrayList<>();
            if (Files.exists(intermediatePartialsPath)) {
                try (var files = Files.list(intermediatePartialsPath)) {
                    List<Path> partialFiles = files.filter(p -> p.toString().endsWith(".txt")).collect(Collectors.toList());
                    for (Path partialFile : partialFiles) {
                        String content = Files.readString(partialFile);
                        models.add(yamlSerializer.deserializeModel(content));
                    }
                }
            }
            OpenApiSpec fullSpec = new OpenApiSpec("Generado desde parciales mergeados", models);
            String fullIntermediateContent = yamlSerializer.serialize(fullSpec);
            FileUtils.writeToFile(basePath.resolve("intermediate/" + finalIntermediateName).toString(), fullIntermediateContent);

            // 2. Merge OpenAPI
            // Re-utilizamos el generador con el fullSpec para asegurar consistencia del YAML final
            String fullOpenApiContent = generator.generate(fullSpec);
            FileUtils.writeToFile(basePath.resolve("openapi/" + finalOpenApiName).toString(), fullOpenApiContent);

        } catch (Exception e) {
            throw new IOException("Error al mergear archivos parciales", e);
        }
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
