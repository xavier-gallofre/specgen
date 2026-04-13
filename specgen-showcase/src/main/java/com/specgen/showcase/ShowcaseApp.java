package com.specgen.showcase;

import com.specgen.core.utils.FileUtils;
import com.specgen.core.utils.Workspace;
import com.specgen.database.exporter.DatabaseExportFileWriter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * Clase principal que demuestra el uso del generador como librería.
 */
public class ShowcaseApp {

    public static void main(String[] args) throws Exception {
        System.out.println("--- Iniciando OpenAPI Generator Showcase ---");

        // 0. Ruta del workspace configurable
        String workspacePath = args.length > 0 ? args[0] : "workspaces/showcase";
        Workspace workspace = new Workspace(workspacePath);

        // 1. Cargar configuración desde el workspace
        Map<String, Object> config = new java.util.HashMap<>();
        workspace.getProperties().forEach((k, v) -> config.put(k.toString(), v));

        // 2. Preparar Base de Datos (Simulando una BD real con H2)
        String url = (String) config.get("hibernate.connection.url");
        if (url == null) {
            url = "jdbc:h2:mem:showcase;DB_CLOSE_DELAY=-1;MODE=Oracle";
            config.put("hibernate.connection.url", url);
            config.put("hibernate.connection.driver_class", "org.h2.Driver");
        }
        
        try (Connection conn = DriverManager.getConnection(url)) {
            String sql = FileUtils.readResource("init.sql");
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            }
            System.out.println("Base de datos inicializada desde init.sql.");
        }

        // 3. Inicializar el escritor de exportación con la configuración cargada
        DatabaseExportFileWriter writer = new DatabaseExportFileWriter(config);
        writer.setWorkspace(workspace);

        // 4. Exportar a archivos parciales y luego mergear
        System.out.println("Generando archivos parciales en el workspace: " + workspacePath);
        writer.exportToPartialFiles(config, List.of("PRODUCTS", "ORDERS", "TEMPLATE_TEST"), null);

        System.out.println("Mezclando archivos parciales...");
        writer.mergePartials(null, "showcase-intermediate.txt", "showcase-openapi.yaml");

        System.out.println("--- Showcase completado con éxito ---");
    }
}
