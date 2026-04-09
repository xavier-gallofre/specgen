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
        // 1. Setup DB H2 in memory with Oracle compatibility and init.sql
        // Importante: No usar 'classpath:' si Hibernate va a intentar abrir la misma URL desde su propio pool.
        // H2 permite RUNSCRIPT de recursos del classpath.
        String url = "jdbc:h2:mem:testdb_spec;DB_CLOSE_DELAY=-1;MODE=Oracle";
        
        try (Connection conn = DriverManager.getConnection(url)) {
            // Ejecutamos el script manualmente para asegurar que está cargado antes de que Hibernate entre
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("RUNSCRIPT FROM 'classpath:init.sql'");
            }

            // Verificar que la tabla existe en PUBLIC
            try (ResultSet rs = conn.getMetaData().getTables(null, "PUBLIC", "CUSTOMERS", null)) {
                if (rs.next()) {
                    System.out.println("[DEBUG_LOG] Tabla CUSTOMERS creada con éxito en PUBLIC vía init.sql");
                } else {
                    System.out.println("[DEBUG_LOG] ADVERTENCIA: Tabla CUSTOMERS no encontrada en PUBLIC tras creación");
                }
            }

            // 2. Inspect DB using Hibernate settings
            DatabaseInspector inspector = new DatabaseInspector();
            java.util.Map<String, Object> settings = new java.util.HashMap<>();
            settings.put("hibernate.connection.url", url);
            settings.put("hibernate.connection.driver_class", "org.h2.Driver");
            // Forzamos el esquema por defecto para evitar INFORMATION_SCHEMA
            settings.put("hibernate.default_schema", "PUBLIC");

            OpenApiSpec spec = inspector.exportFromTables(settings, List.of("CUSTOMERS"));

            // 3. Validate that the pseudo-format is correctly generated
            assertNotNull(spec, "The OpenApiSpec should not be null");
            assertEquals(1, spec.models().size(), "Should have exactly one model");
            assertEquals("CUSTOMERS", spec.models().get(0).name(), "The model name should be CUSTOMERS");
            
            var customerModel = spec.models().get(0);
            assertTrue(customerModel.properties().containsKey("ID"), "Should contain ID property");
            assertTrue(customerModel.properties().containsKey("FIRST_NAME"), "Should contain FIRST_NAME property");
            assertTrue(customerModel.properties().containsKey("LAST_NAME"), "Should contain LAST_NAME property");
            
            assertEquals("number", customerModel.properties().get("ID").type()); // En modo Oracle, H2 mapea NUMBER a number
            assertEquals("string", customerModel.properties().get("FIRST_NAME").type());
            assertEquals(50, customerModel.properties().get("FIRST_NAME").maxLength());
            assertTrue(customerModel.properties().get("FIRST_NAME").required());
            
            System.out.println("[DEBUG_LOG] Generated OpenApiSpec: " + spec);
        }
    }
}
