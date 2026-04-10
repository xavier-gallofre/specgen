package com.openapi.generator.showcase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShowcaseIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    public void runShowcase() throws Exception {
        // Ejecutar la aplicación principal pasando la carpeta temporal
        ShowcaseApp.main(new String[]{tempDir.toString()});

        // 1. Verificar carpetas parciales
        Path intermediatePartialsPath = tempDir.resolve("intermediate/partials");
        Path openapiPartialsPath = tempDir.resolve("openapi/partials");

        assertTrue(Files.exists(intermediatePartialsPath), "La carpeta intermediate/partials debe existir.");
        assertTrue(Files.exists(openapiPartialsPath), "La carpeta openapi/partials debe existir.");

        // 2. Verificar archivos parciales individuales
        assertTrue(Files.exists(intermediatePartialsPath.resolve("PRODUCTS.txt")), "Debe existir el parcial intermedio de PRODUCTS.");
        assertTrue(Files.exists(intermediatePartialsPath.resolve("ORDERS.txt")), "Debe existir el parcial intermedio de ORDERS.");
        assertTrue(Files.exists(openapiPartialsPath.resolve("PRODUCTS.yaml")), "Debe existir el parcial OpenAPI de PRODUCTS.");
        assertTrue(Files.exists(openapiPartialsPath.resolve("ORDERS.yaml")), "Debe existir el parcial OpenAPI de ORDERS.");

        // 3. Verificar archivos finales consolidados (mergeados)
        Path yamlPath = tempDir.resolve("openapi/showcase-openapi.yaml");
        Path intermediatePath = tempDir.resolve("intermediate/showcase-intermediate.txt");

        assertTrue(Files.exists(yamlPath), "El archivo OpenAPI YAML consolidado debe existir.");
        assertTrue(Files.exists(intermediatePath), "El archivo intermedio consolidado debe existir.");

        String yamlContent = Files.readString(yamlPath);
        assertTrue(yamlContent.contains("title: Showcase API"), "El título debe ser el configurado.");
        assertTrue(yamlContent.contains("version: 1.0.1"), "La versión debe ser la configurada.");
        assertTrue(yamlContent.contains("/products:"), "Debe contener el endpoint de products.");
        assertTrue(yamlContent.contains("/orders:"), "Debe contener el endpoint de orders.");
        assertTrue(yamlContent.contains("Modelo para PRODUCTS"), "Debe usar la plantilla personalizada.");

        String intermediateContent = Files.readString(intermediatePath);
        assertTrue(intermediateContent.contains("Generado desde parciales mergeados"), "El info debe indicar que fue mergeado.");
        assertTrue(intermediateContent.contains("name: PRODUCTS"), "Debe contener el modelo PRODUCTS.");
        assertTrue(intermediateContent.contains("name: ORDERS"), "Debe contener el modelo ORDERS.");
        
        System.out.println("[DEBUG_LOG] Showcase YAML content:\n" + yamlContent);
    }
}
