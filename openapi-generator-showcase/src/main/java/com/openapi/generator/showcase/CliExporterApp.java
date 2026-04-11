package com.openapi.generator.showcase;

import com.openapi.generator.core.utils.FileUtils;
import com.openapi.generator.database.exporter.DatabaseExportFileWriter;

import java.io.Console;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Aplicación CLI que permite generar parciales intermedios desde un archivo SQL o una base de datos JDBC.
 */
public class CliExporterApp {

    public static void main(String[] args) {
        if (args.length < 2) {
            printUsage();
            return;
        }

        String mode = args[0];
        String source = args[1];
        String outputDir = args.length > 2 ? args[2] : "openapi-generator-showcase/generated";

        try {
            if ("--sql".equalsIgnoreCase(mode)) {
                handleSqlMode(source, outputDir);
            } else if ("--jdbc".equalsIgnoreCase(mode)) {
                handleJdbcMode(outputDir);
            } else {
                System.out.println("Error: Modo no reconocido '" + mode + "'");
                printUsage();
            }
        } catch (Exception e) {
            System.err.println("Error durante la ejecución: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleSqlMode(String sqlFilePath, String outputDir) throws Exception {
        Path path = Path.of(sqlFilePath);
        if (!Files.exists(path)) {
            throw new IOException("El archivo SQL no existe: " + sqlFilePath);
        }

        String ddl = Files.readString(path);
        System.out.println("Leyendo DDL desde: " + sqlFilePath);

        // Extraer nombres de tablas del DDL (simplificado: buscamos CREATE TABLE)
        List<String> tableNames = extractTableNames(ddl);
        if (tableNames.isEmpty()) {
            System.out.println("No se detectaron sentencias CREATE TABLE en el archivo.");
            return;
        }

        System.out.println("Tablas detectadas: " + tableNames);

        Properties props = FileUtils.loadProperties("application.properties");
        DatabaseExportFileWriter writer = new DatabaseExportFileWriter((Map) props);

        // Nota: SqlInspector actualmente no tiene un método directo para parciales en DatabaseExportFileWriter
        // pero podemos usar el SqlInspector directamente y luego serializar. 
        // Sin embargo, para mantener la consistencia con el flujo de parciales,
        // vamos a añadir un método a DatabaseExportFileWriter o simularlo aquí.
        
        // Vamos a usar una aproximación que use la infraestructura existente.
        com.openapi.generator.database.exporter.SqlInspector sqlInspector = new com.openapi.generator.database.exporter.SqlInspector();
        com.openapi.generator.core.model.OpenApiSpec spec = sqlInspector.exportFromSql(ddl, tableNames);
        
        savePartials(spec, (Map) props, outputDir);
        
        System.out.println("Parciales generados con éxito en: " + outputDir);
    }

    private static void handleJdbcMode(String outputDir) throws Exception {
        Properties props = FileUtils.loadProperties("application.properties");
        
        String user;
        char[] password;
        Scanner scanner = new Scanner(System.in);

        Console console = System.console();
        if (console != null) {
            user = console.readLine("Usuario de base de datos: ");
            password = console.readPassword("Contraseña: ");
        } else {
            // Fallback para entornos sin consola interactiva (como IDEs)
            System.out.print("Usuario de base de datos: ");
            user = scanner.nextLine();
            System.out.print("Contraseña: ");
            password = scanner.nextLine().toCharArray();
        }

        props.setProperty("hibernate.connection.username", user);
        props.setProperty("hibernate.connection.password", new String(password));

        // En un entorno real, pediríamos también las tablas, pero para el showcase
        // usaremos las del init.sql o dejaremos que el usuario las introduzca
        System.out.print("Introduce los nombres de las tablas a exportar (separados por coma): ");
        String tablesInput = scanner.nextLine();
        List<String> tableNames = List.of(tablesInput.split(",")).stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        if (tableNames.isEmpty()) {
            System.out.println("No se indicaron tablas. Abortando.");
            return;
        }

        DatabaseExportFileWriter writer = new DatabaseExportFileWriter((Map) props);
        System.out.println("Conectando a la base de datos y generando parciales...");
        writer.exportToIntermediatePartialFiles((Map) props, tableNames, outputDir);
        
        System.out.println("Parciales generados con éxito en: " + outputDir);
    }

    private static void savePartials(com.openapi.generator.core.model.OpenApiSpec spec, Map<String, Object> config, String outputDir) throws Exception {
        com.openapi.generator.core.utils.YamlSerializer yamlSerializer = new com.openapi.generator.core.utils.YamlSerializer();
        
        Path basePath = Path.of(outputDir);
        Path intermediatePartialsPath = basePath.resolve("intermediate/partials");

        for (com.openapi.generator.core.model.ModelDefinition model : spec.models()) {
            // Parcial Intermedio
            String modelYaml = yamlSerializer.serialize(model);
            FileUtils.writeToFile(intermediatePartialsPath.resolve(model.name() + ".txt").toString(), modelYaml);
        }
    }

    private static List<String> extractTableNames(String ddl) {
        // Implementación simple de extracción de nombres de tablas
        return java.util.Arrays.stream(ddl.split("(?i)CREATE\\s+TABLE"))
                .skip(1)
                .map(part -> {
                    String name = part.trim().split("[\\s\\(\\[]")[0];
                    return name.replaceAll("[\"`\\[\\]]", "");
                })
                .collect(Collectors.toList());
    }

    private static void printUsage() {
        System.out.println("Uso: CliExporterApp <modo> <origen> [directorio_salida]");
        System.out.println("Modos:");
        System.out.println("  --sql <ruta_al_archivo.sql>  : Genera parciales desde un archivo DDL.");
        System.out.println("  --jdbc <cualquier_valor>     : Genera parciales desde una base de datos (pide credenciales).");
        System.out.println("Ejemplo:");
        System.out.println("  java CliExporterApp --sql schema.sql ./generated");
    }
}
