package com.specgen.database.exporter;

import com.specgen.core.utils.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigurableExporterTest {

    @TempDir
    Path tempDir;

    @Test
    public void testExportWithExternalConfig() throws Exception {
        String url = "jdbc:h2:mem:config_db;DB_CLOSE_DELAY=-1;MODE=Oracle";
        try (Connection conn = DriverManager.getConnection(url)) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE CONFIG_TABLE (ID NUMBER(10) PRIMARY KEY, VAL VARCHAR2(100))");
            }

            // 1. Crear archivo de propiedades para la base de datos
            Path dbPropsPath = tempDir.resolve("database.properties");
            String dbPropsContent = "hibernate.connection.url=" + url + "\n" +
                                    "hibernate.connection.driver_class=org.h2.Driver\n" +
                                    "hibernate.default_schema=PUBLIC";
            Files.writeString(dbPropsPath, dbPropsContent);

            // 2. Crear archivo de propiedades para OpenAPI (plantillas)
            Path apiPropsPath = tempDir.resolve("api.properties");
            String apiPropsContent = "api.title=API Configurada\napi.version=1.2.3";
            Files.writeString(apiPropsPath, apiPropsContent);

            // 3. Cargar propiedades
            Properties dbProps = FileUtils.loadProperties(dbPropsPath.toString());
            Map<String, Object> settings = new HashMap<>();
            dbProps.forEach((k, v) -> settings.put(k.toString(), v));

            Properties apiProps = FileUtils.loadProperties(apiPropsPath.toString());
            Map<String, Object> additionalProperties = new HashMap<>();
            apiProps.forEach((k, v) -> additionalProperties.put(k.toString(), v));

            // 4. Configurar plantilla personalizada
            Path templatesDir = tempDir.resolve("custom_templates");
            Files.createDirectories(templatesDir);
            String mainFtl = "openapi: 3.0.3\n" +
                             "info:\n" +
                             "  title: ${.vars['api.title']!'Default Title'}\n" +
                             "  version: ${.vars['api.version']!'1.0.0'}\n" +
                             "paths:\n" +
                             "<#list models as model>\n" +
                             "  /${model.name()?lower_case}:\n" +
                             "    get:\n" +
                             "      summary: Get ${model.name()}\n" +
                             "</#list>";
            Files.writeString(templatesDir.resolve("main.ftl"), mainFtl);
            additionalProperties.put("templates.path", templatesDir.toString());

            // 5. Usar DatabaseExportFileWriter con la configuración
            DatabaseExportFileWriter writer = new DatabaseExportFileWriter(additionalProperties);
            Path outputPath = tempDir.resolve("api.yaml");
            
            writer.exportToYamlFile(settings, List.of("CONFIG_TABLE"), outputPath.toString());

            // 6. Validaciones
            assertTrue(Files.exists(outputPath), "Output file should exist");
            String content = Files.readString(outputPath);
            System.out.println("[DEBUG_LOG] Configured Export Result:\n" + content);
            
            assertTrue(content.contains("title: API Configurada"));
            assertTrue(content.contains("version: 1.2.3"));
            assertTrue(content.contains("/config_table:"));
        }
    }
}
