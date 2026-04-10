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
        DatabaseExportFileWriter writer = new DatabaseExportFileWriter(config);

        // 4. Exportar a archivos parciales y luego mergear
        System.out.println("Generando archivos parciales en carpeta: " + outputDir);
        writer.exportToPartialFiles(config, List.of("PRODUCTS", "ORDERS"), outputDir);

        System.out.println("Mezclando archivos parciales...");
        writer.mergePartials(outputDir, "showcase-intermediate.txt", "showcase-openapi.yaml");

        System.out.println("--- Showcase completado con éxito ---");
    }
}
