package com.openapi.generator.showcase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShowcaseIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    public void testCustomSpringServerGeneration() throws Exception {
        // 1. Generar el OpenAPI primero usando ShowcaseApp
        String outputDir = tempDir.resolve("showcase").toString();
        ShowcaseApp.main(new String[]{outputDir});

        Path openapiPath = tempDir.resolve("showcase/openapi/showcase-openapi.yaml");
        assertTrue(Files.exists(openapiPath), "El archivo OpenAPI debería existir antes de generar el servidor");

        // 2. Ejecutar el generador personalizado
        String springOutputDir = tempDir.resolve("spring-server").toString();
        CustomSpringServerApp.main(new String[]{openapiPath.toString(), springOutputDir});

        // 3. Verificar que se han generado archivos (ej: modelos y apis)
        // Por defecto el generador de Java usa src/main/java
        Path apiFile = tempDir.resolve("spring-server/src/main/java/com/example/api/DefaultApi.java");
        Path modelFile = tempDir.resolve("spring-server/src/main/java/com/example/model/ProductView.java");

        assertTrue(Files.exists(apiFile), "DefaultApi.java debería haberse generado");
        assertTrue(Files.exists(modelFile), "ProductView.java debería haberse generado");

        String apiContent = Files.readString(apiFile);
        assertTrue(apiContent.contains("@RestController"), "Debería contener la anotación @RestController de nuestra plantilla");
        assertTrue(apiContent.contains("SpringBootCustomGenerator"), "Debería mencionar nuestro generador en @Generated");

        // 4. Verificar nuevos archivos (Repository y Service por cada modelo)
        // Como se generan por modelo (ProductView, ProductForm, etc.)
        Path productViewRepo = tempDir.resolve("spring-server/src/main/java/com/example/model/ProductViewRepository.java");
        Path productViewService = tempDir.resolve("spring-server/src/main/java/com/example/model/ProductViewService.java");

        assertTrue(Files.exists(productViewRepo), "ProductViewRepository.java debería haberse generado");
        assertTrue(Files.exists(productViewService), "ProductViewService.java debería haberse generado");

        String serviceContent = Files.readString(productViewService);
        assertTrue(serviceContent.contains("@Service"), "El servicio debería tener la anotación @Service");
        assertTrue(serviceContent.contains("ProductViewRepository repository"), "El servicio debería inyectar el repositorio");

        String modelContent = Files.readString(modelFile);
        System.out.println("[DEBUG_LOG] Model Content:\n" + modelContent);
        assertTrue(modelContent.contains("@Entity"), "El modelo debería ser una entidad JPA");
        assertTrue(modelContent.contains("@Id"), "El modelo debería tener una clave primaria");
    }

    @Test
    public void runShowcase() throws Exception {
        // Ejecutar la aplicación principal pasando la carpeta temporal
        ShowcaseApp.main(new String[]{tempDir.toString()});
        verifyResults(tempDir);
    }

    @Test
    public void testSplitApps() throws Exception {
        Path splitDir = tempDir.resolve("split");
        Files.createDirectories(splitDir);
        
        // 1. Ejecutar Generador de Intermedios
        IntermediateGeneratorApp.main(new String[]{splitDir.toString()});

        // Verificar que existen parciales intermedios pero NO el mergeado final
        Path intermediatePartialsPath = splitDir.resolve("intermediate/partials");
        Path finalYamlPath = splitDir.resolve("openapi/showcase-openapi.yaml");

        assertTrue(Files.exists(intermediatePartialsPath), "La carpeta intermediate/partials debe existir.");
        assertTrue(Files.exists(intermediatePartialsPath.resolve("PRODUCTS.txt")), "Debe existir el parcial intermedio de PRODUCTS.");
        assertTrue(!Files.exists(finalYamlPath), "El archivo final NO debería existir aún.");

        // 2. Ejecutar Mezclador
        OpenApiMergeApp.main(new String[]{splitDir.toString()});

        // Ahora verificar resultados completos
        verifyResults(splitDir);
    }

    @Test
    public void testCliExporterSql() throws Exception {
        Path cliDir = tempDir.resolve("cli-sql");
        Files.createDirectories(cliDir);

        // Crear un archivo SQL temporal
        Path sqlFile = tempDir.resolve("test.sql");
        String ddl = "CREATE TABLE USERS (ID INTEGER PRIMARY KEY, NAME VARCHAR(100)); " +
                     "CREATE TABLE ROLES (ID INTEGER PRIMARY KEY, ROLE_NAME VARCHAR(50));";
        Files.writeString(sqlFile, ddl);

        // Ejecutar CliExporterApp en modo --sql
        CliExporterApp.main(new String[]{"--sql", sqlFile.toString(), cliDir.toString()});

        // Verificar que se crearon los parciales para USERS y ROLES
        Path intermediatePartialsPath = cliDir.resolve("intermediate/partials");
        Path openapiPartialsPath = cliDir.resolve("openapi/partials");

        assertTrue(Files.exists(intermediatePartialsPath.resolve("USERS.txt")), "Debe existir el parcial intermedio de USERS.");
        assertTrue(Files.exists(intermediatePartialsPath.resolve("ROLES.txt")), "Debe existir el parcial intermedio de ROLES.");
        assertTrue(!Files.exists(openapiPartialsPath.resolve("USERS.yaml")), "El parcial OpenAPI de USERS NO debería existir.");
        assertTrue(!Files.exists(openapiPartialsPath.resolve("ROLES.yaml")), "El parcial OpenAPI de ROLES NO debería existir.");
    }

    private void verifyResults(Path outputBaseDir) throws Exception {
        // 1. Verificar carpetas parciales
        Path intermediatePartialsPath = outputBaseDir.resolve("intermediate/partials");
        Path openapiPartialsPath = outputBaseDir.resolve("openapi/partials");

        assertTrue(Files.exists(intermediatePartialsPath), "La carpeta intermediate/partials debe existir.");
        assertTrue(Files.exists(openapiPartialsPath), "La carpeta openapi/partials debe existir.");

        // 2. Verificar archivos parciales individuales
        assertTrue(Files.exists(intermediatePartialsPath.resolve("PRODUCTS.txt")), "Debe existir el parcial intermedio de PRODUCTS.");
        assertTrue(Files.exists(intermediatePartialsPath.resolve("ORDERS.txt")), "Debe existir el parcial intermedio de ORDERS.");
        assertTrue(Files.exists(openapiPartialsPath.resolve("PRODUCTS.yaml")), "Debe existir el parcial OpenAPI de PRODUCTS.");
        assertTrue(Files.exists(openapiPartialsPath.resolve("ORDERS.yaml")), "Debe existir el parcial OpenAPI de ORDERS.");

        // Verificar nuevos fragmentos
        assertTrue(Files.exists(openapiPartialsPath.resolve("PRODUCTS_paths.yaml")), "Debe existir el fragmento de paths de PRODUCTS.");
        assertTrue(Files.exists(openapiPartialsPath.resolve("PRODUCTS_schemas.yaml")), "Debe existir el fragmento de schemas de PRODUCTS.");
        assertTrue(Files.exists(openapiPartialsPath.resolve("ORDERS_paths.yaml")), "Debe existir el fragmento de paths de ORDERS.");
        assertTrue(Files.exists(openapiPartialsPath.resolve("ORDERS_schemas.yaml")), "Debe existir el fragmento de schemas de ORDERS.");

        // 3. Verificar archivos finales consolidados (mergeados)
        Path yamlPath = outputBaseDir.resolve("openapi/showcase-openapi.yaml");
        Path intermediatePath = outputBaseDir.resolve("intermediate/showcase-intermediate.txt");

        assertTrue(Files.exists(yamlPath), "El archivo OpenAPI YAML consolidado debe existir.");
        assertTrue(Files.exists(intermediatePath), "El archivo intermedio consolidado debe existir.");

        String yamlContent = Files.readString(yamlPath);
        assertTrue(yamlContent.contains("title: Showcase API"), "El título debe ser el configurado.");
        assertTrue(yamlContent.contains("version: 1.0.1"), "La versión debe ser la configurada.");
        assertTrue(yamlContent.contains("/products:"), "Debe contener el endpoint de products.");
        assertTrue(yamlContent.contains("/orders:"), "Debe contener el endpoint de orders.");
        assertTrue(yamlContent.contains("/products/{id}:"), "Debe contener el endpoint de products por id.");
        assertTrue(yamlContent.contains("/orders/{id}:"), "Debe contener el endpoint de orders por id.");
        assertTrue(yamlContent.contains("OrderView:"), "Debe contener el schema OrderView.");
        assertTrue(yamlContent.contains("OrderForm:"), "Debe contener el schema OrderForm.");
        assertTrue(yamlContent.contains("ProductView:"), "Debe contener el schema ProductView.");
        assertTrue(yamlContent.contains("ProductForm:"), "Debe contener el schema ProductForm.");
        assertTrue(yamlContent.contains("Modelo de vista para PRODUCTS"), "Debe usar la plantilla de vista personalizada.");
        assertTrue(yamlContent.contains("Modelo de formulario para PRODUCTS"), "Debe usar la plantilla de formulario personalizada.");

        String intermediateContent = Files.readString(intermediatePath);
        assertTrue(intermediateContent.contains("Generado desde parciales mergeados"), "El info debe indicar que fue mergeado.");
        assertTrue(intermediateContent.contains("name: PRODUCTS"), "Debe contener el modelo PRODUCTS.");
        assertTrue(intermediateContent.contains("name: ORDERS"), "Debe contener el modelo ORDERS.");
    }
}
