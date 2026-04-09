package com.openapi.generator.database.exporter;

import com.openapi.generator.core.model.OpenApiSpec;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseExporterIntegrationTest {

    @Test
    public void testExportTableToOpenApiSpec() throws Exception {
        // 1. Setup DB H2 in memory
        String url = "jdbc:h2:mem:testdb_spec;DB_CLOSE_DELAY=-1";
        // Usamos una conexión persistente para evitar que la DB desaparezca entre pasos si DB_CLOSE_DELAY no funcionara como esperamos
        try (Connection conn = DriverManager.getConnection(url)) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE USERS (ID INT PRIMARY KEY, NAME VARCHAR(100) NOT NULL, EMAIL VARCHAR(255))");
                // Verificar que la tabla existe en PUBLIC
                try (ResultSet rs = conn.getMetaData().getTables(null, "PUBLIC", "USERS", null)) {
                    if (rs.next()) {
                        System.out.println("[DEBUG_LOG] Tabla USERS creada con éxito en PUBLIC");
                    } else {
                        System.out.println("[DEBUG_LOG] ADVERTENCIA: Tabla USERS no encontrada en PUBLIC tras creación");
                    }
                }
            }

            // 2. Inspect DB using Hibernate settings
            DatabaseInspector inspector = new DatabaseInspector();
            java.util.Map<String, Object> settings = new java.util.HashMap<>();
            settings.put("hibernate.connection.url", url);
            settings.put("hibernate.connection.driver_class", "org.h2.Driver");
            // Forzamos el esquema por defecto para evitar INFORMATION_SCHEMA
            settings.put("hibernate.default_schema", "PUBLIC");

            OpenApiSpec spec = inspector.exportFromTables(settings, List.of("USERS"));

            // 3. Validate that the pseudo-format is correctly generated
            assertNotNull(spec, "The OpenApiSpec should not be null");
            assertEquals(1, spec.models().size(), "Should have exactly one model");
            assertEquals("USERS", spec.models().get(0).name(), "The model name should be USERS");
            
            var userModel = spec.models().get(0);
            assertTrue(userModel.properties().containsKey("ID"), "Should contain ID property");
            assertTrue(userModel.properties().containsKey("NAME"), "Should contain NAME property");
            assertTrue(userModel.properties().containsKey("EMAIL"), "Should contain EMAIL property");
            
            assertEquals("integer", userModel.properties().get("ID").type());
            assertEquals("string", userModel.properties().get("NAME").type());
            assertEquals(100, userModel.properties().get("NAME").maxLength());
            assertTrue(userModel.properties().get("NAME").required());
            
            System.out.println("[DEBUG_LOG] Generated OpenApiSpec: " + spec);
        }
    }
}
