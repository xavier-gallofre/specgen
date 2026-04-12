package com.specgen.database.exporter;

import com.specgen.core.model.OpenApiSpec;
import com.specgen.core.model.PropertyDefinition;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class OracleDatabaseExporterTest {

    @Container
    private static final OracleContainer oracle = new OracleContainer("gvenzl/oracle-xe:21-slim-faststart")
            .withStartupTimeoutSeconds(300)
            .withConnectTimeoutSeconds(120)
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("init.sql"),
                    "/container-entrypoint-initdb.d/init.sql"
            );

    @Test
    public void testExportFromOracle() throws Exception {
        // 1. Obtener parámetros desde Testcontainers
        String jdbcUrl = oracle.getJdbcUrl();
        String username = oracle.getUsername();
        String password = oracle.getPassword();

        // Aseguramos que el script se haya ejecutado (Oracle puede tardar un poco tras 'startup')
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            System.out.println("[DEBUG_LOG] Conectado a Oracle para verificación manual...");
            try (var rs = conn.getMetaData().getTables(null, username.toUpperCase(), "CUSTOMERS", null)) {
                if (rs.next()) {
                    System.out.println("[DEBUG_LOG] Tabla CUSTOMERS encontrada en Oracle");
                } else {
                    System.out.println("[DEBUG_LOG] ¡ADVERTENCIA! Tabla CUSTOMERS NO encontrada en Oracle. Reintentando creación...");
                    try (var stmt = conn.createStatement()) {
                        stmt.execute("CREATE TABLE CUSTOMERS (ID NUMBER(10) PRIMARY KEY, FIRST_NAME VARCHAR2(50) NOT NULL, LAST_NAME VARCHAR2(50), AGE NUMBER(3), CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
                    }
                }
            }
        }

        // 2. Ejecutar inspección usando Hibernate settings
        DatabaseInspector inspector = new DatabaseInspector();
        java.util.Map<String, Object> settings = new java.util.HashMap<>();
        settings.put("hibernate.connection.url", jdbcUrl);
        settings.put("hibernate.connection.username", username);
        settings.put("hibernate.connection.password", password);
        settings.put("hibernate.connection.driver_class", oracle.getDriverClassName());
        settings.put("hibernate.dialect", "org.hibernate.dialect.OracleDialect");
        // Forzamos el esquema del usuario por defecto de Testcontainers (suele ser SYSTEM o TEST si se define)
        // Pero Oracle lo devuelve en mayúsculas
        settings.put("hibernate.default_schema", username.toUpperCase());

        OpenApiSpec spec = inspector.exportFromTables(settings, List.of("CUSTOMERS"));

        // 3. Validaciones
        assertNotNull(spec);
        assertEquals(1, spec.models().size());
        var customerModel = spec.models().get(0);

        // Log de las propiedades encontradas para depuración
        System.out.println("[DEBUG_LOG] Propiedades encontradas: " + customerModel.properties().keySet());

        assertEquals("CUSTOMERS", customerModel.name());

        // Oracle devuelve nombres en MAYÚSCULAS por defecto
        assertTrue(customerModel.properties().containsKey("ID"), "Debe contener ID");
        assertTrue(customerModel.properties().containsKey("FIRST_NAME"), "Debe contener FIRST_NAME");
        assertTrue(customerModel.properties().containsKey("LAST_NAME"), "Debe contener LAST_NAME");
        assertTrue(customerModel.properties().containsKey("AGE"), "Debe contener AGE");
        assertTrue(customerModel.properties().containsKey("CREATED_AT"), "Debe contener CREATED_AT");

        PropertyDefinition idProp = customerModel.properties().get("ID");
        assertEquals("number", idProp.type()); // Oracle NUMBER suele mapearse a number o integer según escala

        PropertyDefinition firstNameProp = customerModel.properties().get("FIRST_NAME");
        assertEquals("string", firstNameProp.type());
        assertEquals(50, firstNameProp.maxLength());
        assertTrue(firstNameProp.required());

        System.out.println("[DEBUG_LOG] Oracle Spec: " + spec);
    }
}
