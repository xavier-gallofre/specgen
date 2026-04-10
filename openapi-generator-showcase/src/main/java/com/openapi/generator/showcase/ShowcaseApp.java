package com.openapi.generator.showcase;

import com.openapi.generator.core.utils.FileUtils;
import com.openapi.generator.database.exporter.DatabaseExportFileWriter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Clase principal que demuestra el uso del generador como librería.
 */
public class ShowcaseApp {

    public static void main(String[] args) throws Exception {
        System.out.println("--- Iniciando OpenAPI Generator Showcase ---");

        // 0. Ruta de salida configurable
        String outputDir = args.length > 0 ? args[0] : "openapi-generator-showcase/generated";

        // 1. Cargar configuración
        Properties props = FileUtils.loadProperties("application.properties");
        Map<String, Object> config = (Map) props;

        // 2. Preparar Base de Datos (Simulando una BD real con H2)
        String url = (String) config.get("hibernate.connection.url");
        try (Connection conn = DriverManager.getConnection(url)) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE PRODUCTS (ID NUMBER(10) PRIMARY KEY, NAME VARCHAR2(100), PRICE NUMBER(10,2))");
                stmt.execute("CREATE TABLE ORDERS (ID NUMBER(10) PRIMARY KEY, PRODUCT_ID NUMBER(10), QUANTITY NUMBER(5))");
            }
            System.out.println("Base de datos inicializada con tablas PRODUCTS y ORDERS.");
        }

        // 3. Inicializar el escritor de exportación con la configuración cargada
        // Esto usará las plantillas definidas en templates.path y las propiedades de api.*
        DatabaseExportFileWriter writer = new DatabaseExportFileWriter(config);

        // 4. Exportar metadatos a carpeta de salida
        String outputYaml = outputDir + "/showcase-openapi.yaml";
        String outputIntermediate = outputDir + "/showcase-intermediate.txt";

        System.out.println("Generando especificación OpenAPI en: " + outputYaml);
        writer.exportToYamlFile(config, List.of("PRODUCTS", "ORDERS"), outputYaml);

        System.out.println("Generando formato intermedio en: " + outputIntermediate);
        writer.exportToIntermediateFile(config, List.of("PRODUCTS", "ORDERS"), outputIntermediate);

        System.out.println("--- Showcase completado con éxito ---");
    }
}
