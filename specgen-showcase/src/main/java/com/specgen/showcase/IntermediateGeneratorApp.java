package com.specgen.showcase;

import com.specgen.core.utils.FileUtils;
import com.specgen.database.exporter.DatabaseExportFileWriter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Aplicación que genera únicamente los archivos intermedios parciales a partir de la inspección de la base de datos.
 */
public class IntermediateGeneratorApp {

    public static void main(String[] args) throws Exception {
        System.out.println("--- Iniciando Generador de Intermedios (Parciales) ---");

        // 0. Ruta de salida configurable
        String outputDir = args.length > 0 ? args[0] : "specgen-showcase/generated";

        // 1. Cargar configuración
        Properties props = FileUtils.loadProperties("application.properties");
        Map<String, Object> config = (Map) props;

        // 2. Preparar Base de Datos (H2)
        String url = (String) config.get("hibernate.connection.url");
        try (Connection conn = DriverManager.getConnection(url)) {
            String sql = FileUtils.readResource("init.sql");
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            }
            System.out.println("Base de datos inicializada desde init.sql.");
        }

        // 3. Inicializar el escritor y exportar parciales
        DatabaseExportFileWriter writer = new DatabaseExportFileWriter(config);
        
        System.out.println("Generando archivos parciales en: " + outputDir);
        writer.exportToPartialFiles(config, List.of("PRODUCTS", "ORDERS"), outputDir);

        System.out.println("--- Generación de parciales completada con éxito ---");
    }
}
