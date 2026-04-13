package com.specgen.database.exporter;

import com.specgen.core.OpenApiGenerator;
import com.specgen.core.model.ModelDefinition;
import com.specgen.core.model.OpenApiSpec;
import com.specgen.core.utils.FileUtils;
import com.specgen.core.utils.YamlSerializer;

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
    private ExportRuleManager ruleManager;

    public DatabaseExportFileWriter() {
        this(new java.util.HashMap<>());
    }

    public void setDictionary(NameDictionary dictionary) {
        this.inspector.setDictionary(dictionary);
        this.sqlInspector.setDictionary(dictionary);
    }

    public void setRuleManager(ExportRuleManager ruleManager) {
        this.ruleManager = ruleManager;
        this.inspector.setRuleManager(ruleManager);
        this.sqlInspector.setRuleManager(ruleManager);
    }

    private void writeReviewReport(String baseOutputDir) throws IOException {
        if (ruleManager == null || ruleManager.getReviewLog().isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("# Informe de Revisión de Exportación\n\n");
        sb.append("Se han aplicado reglas automáticas que requieren revisión manual para asegurar la integridad de los datos.\n\n");
        sb.append("| Tabla | Columna | Riesgo / Explicación | Enlace al Parcial |\n");
        sb.append("|-------|---------|----------------------|-------------------|\n");

        for (ExportRuleManager.ReviewEntry entry : ruleManager.getReviewLog()) {
            String partialLink = "intermediate/partials/" + entry.tableName() + ".txt";
            sb.append(String.format("| %s | %s | %s | [Ver parcial](%s) |\n", 
                entry.tableName(), entry.columnName(), entry.note(), partialLink));
        }

        FileUtils.writeToFile(Path.of(baseOutputDir).resolve("revisar.md").toString(), sb.toString());
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

                // 2. Parciales OpenAPI Fragmentados
                // a. Fragmento de Paths
                String pathsFragment = generator.generateFragment(spec, model.name(), "paths_crud.ftl");
                FileUtils.writeToFile(openapiPartialsPath.resolve(model.name() + "_paths.yaml").toString(), pathsFragment);

                // b. Fragmento de Schemas
                String schemasFragment = generator.generateFragment(spec, model.name(), "schemas.ftl");
                FileUtils.writeToFile(openapiPartialsPath.resolve(model.name() + "_schemas.yaml").toString(), schemasFragment);

                // 3. Parcial OpenAPI Combinado (Mantiene compatibilidad con el formato esperado por otros procesos)
                String partialOpenApi = "paths:\n" + pathsFragment + "\ncomponents:\n" + schemasFragment;
                FileUtils.writeToFile(openapiPartialsPath.resolve(model.name() + ".yaml").toString(), partialOpenApi);
            }
            writeReviewReport(baseOutputDir);
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
            writeReviewReport(baseOutputDir);
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
