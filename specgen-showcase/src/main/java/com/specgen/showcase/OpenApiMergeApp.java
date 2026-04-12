package com.specgen.showcase;

import com.specgen.core.utils.FileUtils;
import com.specgen.database.exporter.DatabaseExportFileWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/**
 * Aplicación que a partir de los archivos intermedios existentes (comprobando su presencia)
 * genera el archivo OpenAPI final.
 */
public class OpenApiMergeApp {

    public static void main(String[] args) throws Exception {
        System.out.println("--- Iniciando Mezclador de Parciales a OpenAPI ---");

        // 0. Ruta de entrada/salida configurable
        String outputDir = args.length > 0 ? args[0] : "specgen-showcase/generated";

        // 1. Cargar configuración (para inyectar plantillas si es necesario)
        Properties props = FileUtils.loadProperties("application.properties");
        Map<String, Object> config = (Map) props;

        // 2. Comprobar que existen los archivos intermedios parciales
        Path intermediatePartialsPath = Path.of(outputDir).resolve("intermediate/partials");
        if (!Files.exists(intermediatePartialsPath) || !Files.isDirectory(intermediatePartialsPath)) {
            throw new IOException("No se han encontrado archivos parciales intermedios en: " + intermediatePartialsPath.toAbsolutePath());
        }

        long count;
        try (var files = Files.list(intermediatePartialsPath)) {
            count = files.filter(p -> p.toString().endsWith(".txt")).count();
        }

        if (count == 0) {
            throw new IOException("La carpeta de parciales intermedios está vacía: " + intermediatePartialsPath.toAbsolutePath());
        }

        System.out.println("Se han detectado " + count + " archivos parciales intermedios.");

        // 3. Inicializar el escritor y realizar el merge
        DatabaseExportFileWriter writer = new DatabaseExportFileWriter(config);

        System.out.println("Mezclando archivos parciales en carpeta: " + outputDir);
        writer.mergePartials(outputDir, "showcase-intermediate.txt", "showcase-openapi.yaml");

        System.out.println("--- Mezclado completado con éxito ---");
    }
}
