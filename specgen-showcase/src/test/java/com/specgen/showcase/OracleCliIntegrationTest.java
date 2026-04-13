package com.specgen.showcase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class OracleCliIntegrationTest {

    @Container
    private static final OracleContainer oracle = new OracleContainer("gvenzl/oracle-xe:21-slim-faststart")
            .withStartupTimeoutSeconds(300)
            .withConnectTimeoutSeconds(120);

    @TempDir
    Path tempDir;

    @Test
    public void testCliExporterJdbcWithOracle() throws Exception {
        // 1. Preparar la base de datos Oracle
        String jdbcUrl = oracle.getJdbcUrl();
        String username = oracle.getUsername();
        String password = oracle.getPassword();

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE EMPLOYEES (ID NUMBER(10) PRIMARY KEY, NAME VARCHAR2(100) NOT NULL, EMAIL VARCHAR2(100))");
        }

        // 2. Preparar el archivo application.properties temporal
        // CliExporterApp carga "application.properties" del classpath o del directorio actual.
        // Como estamos en un test, vamos a crear uno en el directorio actual (raíz del proyecto durante el test)
        // y asegurarnos de borrarlo después, o mejor, modificar CliExporterApp para que sea más flexible,
        // pero la instrucción dice probar la opción del CLI tal cual.
        
        // El CliExporterApp usa FileUtils.loadProperties("application.properties")
        // Vamos a crear un application.properties temporal en la raíz para el test.
        Path propsPath = Path.of("application.properties").toAbsolutePath();
        Properties props = new Properties();
        props.setProperty("hibernate.connection.url", jdbcUrl);
        props.setProperty("hibernate.connection.username", username);
        props.setProperty("hibernate.connection.password", password);
        props.setProperty("hibernate.connection.driver_class", oracle.getDriverClassName());
        props.setProperty("hibernate.dialect", "org.hibernate.dialect.OracleDialect");
        props.setProperty("hibernate.default_schema", username.toUpperCase());
        // Forzar dialecto explícito para evitar problemas de detección automática en entornos restrictivos
        props.setProperty("hibernate.dialect", "org.hibernate.dialect.OracleDialect");
        props.setProperty("jakarta.persistence.jdbc.driver", oracle.getDriverClassName());
        props.setProperty("jakarta.persistence.jdbc.url", jdbcUrl);
        props.setProperty("jakarta.persistence.jdbc.user", username);
        props.setProperty("jakarta.persistence.jdbc.password", password);
        
        // Guardar propiedades originales si existen para restaurarlas
        String originalProps = null;
        if (Files.exists(propsPath)) {
            originalProps = Files.readString(propsPath);
        }
        
        try {
            java.io.Writer propsWriter = Files.newBufferedWriter(propsPath);
            props.store(propsWriter, "Test properties");
            propsWriter.close();
            
            // 3. Simular la entrada del usuario (User, Password, Tables)
            String input = username + "\n" + password + "\nEMPLOYEES\n";
            InputStream originalIn = System.in;
            System.setIn(new ByteArrayInputStream(input.getBytes()));

            try {
                // 4. Ejecutar CLI con ruta de propiedades explícita
                String outputDir = tempDir.resolve("generated").toString();
                CliExporterApp.main(new String[]{
                    "--jdbc", outputDir, 
                    "--properties", propsPath.toString()
                });

                // 5. Verificar resultados
                Path partialPath = tempDir.resolve("generated/intermediate/partials/EMPLOYEES.txt");
                assertTrue(Files.exists(partialPath), "El archivo parcial de EMPLOYEES debería existir");
                
                String content = Files.readString(partialPath);
                assertTrue(content.contains("name: EMPLOYEES"), "El contenido debe mencionar la tabla EMPLOYEES");
                assertTrue(content.contains("NAME"), "El contenido debe contener la columna NAME");
                assertTrue(content.contains("EMAIL"), "El contenido debe contener la columna EMAIL");

            } finally {
                System.setIn(originalIn);
            }
        } finally {
            // Restaurar o borrar application.properties
            if (originalProps != null) {
                Files.writeString(propsPath, originalProps);
            } else {
                Files.deleteIfExists(propsPath);
            }
        }
    }
}
