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

        // Verificar que los archivos se crearon en la carpeta temporal
        Path yamlPath = tempDir.resolve("showcase-openapi.yaml");
        Path intermediatePath = tempDir.resolve("showcase-intermediate.txt");

        assertTrue(Files.exists(yamlPath), "El archivo OpenAPI YAML debe existir.");
        assertTrue(Files.exists(intermediatePath), "El archivo intermedio debe existir.");

        String yamlContent = Files.readString(yamlPath);
        assertTrue(yamlContent.contains("title: Showcase API"), "El título debe ser el configurado.");
        assertTrue(yamlContent.contains("version: 1.0.1"), "La versión debe ser la configurada.");
        assertTrue(yamlContent.contains("/products:"), "Debe contener el endpoint de products.");
        assertTrue(yamlContent.contains("/orders:"), "Debe contener el endpoint de orders.");
        assertTrue(yamlContent.contains("Modelo para PRODUCTS"), "Debe usar la plantilla personalizada.");
        
        System.out.println("[DEBUG_LOG] Showcase YAML content:\n" + yamlContent);
    }
}
