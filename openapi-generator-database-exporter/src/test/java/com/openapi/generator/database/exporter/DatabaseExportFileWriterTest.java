package com.openapi.generator.database.exporter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DatabaseExportFileWriterTest {

    @TempDir
    Path tempDir;

    @Test
    public void testExportToFile() throws IOException, Exception {
        String url = "jdbc:h2:mem:test_file_db;DB_CLOSE_DELAY=-1;MODE=Oracle";
        try (Connection conn = DriverManager.getConnection(url)) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE TEST_TABLE (ID NUMBER(10) PRIMARY KEY, NAME VARCHAR2(100))");
            }

            Map<String, Object> settings = new HashMap<>();
            settings.put("hibernate.connection.url", url);
            settings.put("hibernate.connection.driver_class", "org.h2.Driver");
            settings.put("hibernate.default_schema", "PUBLIC");

            DatabaseExportFileWriter writer = new DatabaseExportFileWriter();
            
            Path yamlPath = tempDir.resolve("output.yaml");
            Path intermediatePath = tempDir.resolve("intermediate.txt");

            writer.exportToYamlFile(settings, List.of("TEST_TABLE"), yamlPath.toString());
            writer.exportToIntermediateFile(settings, List.of("TEST_TABLE"), intermediatePath.toString());

            assertTrue(Files.exists(yamlPath), "YAML file should exist");
            String yamlContent = Files.readString(yamlPath);
            assertTrue(yamlContent.contains("openapi: 3.0.3"), "YAML should be a valid OpenAPI spec");
            assertTrue(yamlContent.contains("/test_tables:"), "YAML should contain generated path");

            assertTrue(Files.exists(intermediatePath), "Intermediate file should exist");
            String intermediateContent = Files.readString(intermediatePath);
            assertTrue(intermediateContent.contains("name: TEST_TABLE"), "Intermediate file should contain table name in YAML format");
            assertTrue(intermediateContent.contains("info:"), "Intermediate file should be a valid YAML representing OpenApiSpec");
            assertTrue(intermediateContent.contains("models:"), "Intermediate file should contain models key");
        }
    }
}
