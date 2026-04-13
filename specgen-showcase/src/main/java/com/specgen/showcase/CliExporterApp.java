package com.specgen.showcase;

import com.specgen.core.utils.FileUtils;
import com.specgen.core.utils.Workspace;
import com.specgen.database.exporter.DatabaseExportFileWriter;

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
        if (args.length < 1) {
            printUsage();
            return;
        }

        String mode = null;
        String source = null;
        String workspacePath = null;
        String explicitPropsPath = null;

        for (int i = 0; i < args.length; i++) {
            if ("--workspace".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                workspacePath = args[++i];
            } else if ("--properties".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                explicitPropsPath = args[++i];
            } else if (args[i].startsWith("--") && mode == null) {
                mode = args[i];
                if (i + 1 < args.length) {
                    source = args[++i];
                }
            } else if (source == null) {
                source = args[i];
            }
        }

        if (mode == null) {
            printUsage();
            return;
        }

        try {
            workspacePath = workspacePath != null ? workspacePath : "workspaces/showcase";
            Workspace workspace = workspacePath != null ? new Workspace(workspacePath) : null;

            if ("--sql".equalsIgnoreCase(mode)) {
                handleSqlMode(source, workspace, explicitPropsPath);
            } else if ("--jdbc".equalsIgnoreCase(mode)) {
                // Si no hay workspace, el argumento después de --jdbc podría ser el outputDir
                // Pero con el nuevo parseo, source sería ese argumento si no hay workspace
                String outputDir = (workspace == null) ? source : null;
                handleJdbcMode(workspace, outputDir, explicitPropsPath);
            } else {
                System.out.println("Error: Modo no reconocido '" + mode + "'");
                printUsage();
            }
        } catch (Exception e) {
            System.err.println("Error durante la ejecución: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleSqlMode(String sqlFilePath, Workspace workspace, String explicitPropsPath) throws Exception {
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

        Map<String, Object> propsMap = loadConfiguration(workspace, explicitPropsPath);

        DatabaseExportFileWriter writer = new DatabaseExportFileWriter(propsMap);
        if (workspace != null) {
            writer.setWorkspace(workspace);
        }

        com.specgen.database.exporter.SqlInspector sqlInspector = new com.specgen.database.exporter.SqlInspector();
        com.specgen.core.model.OpenApiSpec spec = sqlInspector.exportFromSql(ddl, tableNames);
        
        savePartials(spec, propsMap, workspace);
        
        System.out.println("Parciales generados con éxito");
    }

    private static void handleJdbcMode(Workspace workspace, String outputDir, String explicitPropsPath) throws Exception {
        Map<String, Object> propsMap = loadConfiguration(workspace, explicitPropsPath);
        
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

        propsMap.put("hibernate.connection.username", user);
        propsMap.put("hibernate.connection.password", new String(password));
        
        // Sincronizar con jakarta por si acaso
        propsMap.put("jakarta.persistence.jdbc.user", user);
        propsMap.put("jakarta.persistence.jdbc.password", new String(password));
        if (propsMap.containsKey("hibernate.connection.url")) {
            propsMap.put("jakarta.persistence.jdbc.url", propsMap.get("hibernate.connection.url"));
        }
        if (propsMap.containsKey("hibernate.connection.driver_class")) {
            propsMap.put("jakarta.persistence.jdbc.driver", propsMap.get("hibernate.connection.driver_class"));
        }

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

        DatabaseExportFileWriter writer = new DatabaseExportFileWriter(propsMap);
        if (workspace != null) {
            writer.setWorkspace(workspace);
        }
        System.out.println("Conectando a la base de datos y generando parciales...");
        writer.exportToIntermediatePartialFiles(propsMap, tableNames, outputDir);
        
        System.out.println("Parciales generados con éxito");
    }

    private static Map<String, Object> loadConfiguration(Workspace workspace, String explicitPropsPath) throws IOException {
        Map<String, Object> propsMap = new java.util.HashMap<>();
        if (workspace != null) {
            workspace.getProperties().forEach((k, v) -> propsMap.put(k.toString(), v));
        } else {
            Properties props = new Properties();
            if (explicitPropsPath != null) {
                Path path = Path.of(explicitPropsPath).toAbsolutePath();
                if (Files.exists(path)) {
                    try (var is = Files.newInputStream(path)) {
                        props.load(is);
                    }
                } else {
                    throw new IOException("El archivo de propiedades explícito no existe: " + explicitPropsPath);
                }
            } else {
                // Carga por defecto
                String[] propsFileNames = {"application.properties", "specgen-showcase/application.properties"};
                boolean loaded = false;
                for (String fileName : propsFileNames) {
                    Path path = Path.of(fileName).toAbsolutePath();
                    if (Files.exists(path)) {
                        try (var is = Files.newInputStream(path)) {
                            props.load(is);
                            loaded = true;
                            break;
                        } catch (IOException ignored) {}
                    }
                }
                if (!loaded) {
                    try {
                        props = FileUtils.loadProperties("application.properties");
                    } catch (Exception ignored) {}
                }
            }
            props.forEach((k, v) -> propsMap.put(k.toString(), v.toString()));
        }
        return propsMap;
    }

    private static void savePartials(com.specgen.core.model.OpenApiSpec spec, Map<String, Object> config, Workspace workspace) throws Exception {
        com.specgen.core.utils.YamlSerializer yamlSerializer = new com.specgen.core.utils.YamlSerializer();
        
        Path basePath = workspace != null ? workspace.getGeneratedPath() : Path.of("generated");
        Path intermediatePartialsPath = basePath.resolve("intermediate/partials");

        for (com.specgen.core.model.ModelDefinition model : spec.models()) {
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
        System.out.println("Uso: CliExporterApp [--workspace <ruta>] [--properties <ruta>] <modo> <origen>");
        System.out.println("Opciones:");
        System.out.println("  --workspace <ruta>           : Define la zona de trabajo (templates, dictionary, properties).");
        System.out.println("  --properties <ruta>          : Archivo de propiedades de base de datos (alternativo a application.properties).");
        System.out.println("Modos:");
        System.out.println("  --sql <ruta_al_archivo.sql>  : Genera parciales desde un archivo DDL.");
        System.out.println("  --jdbc <output_dir>          : Genera parciales desde una base de datos (pide credenciales).");
        System.out.println("Ejemplo:");
        System.out.println("  java CliExporterApp --workspace ./workspaces/showcase --sql schema.sql");
        System.out.println("  java CliExporterApp --jdbc ./generated --properties my-db.properties");
    }
}
