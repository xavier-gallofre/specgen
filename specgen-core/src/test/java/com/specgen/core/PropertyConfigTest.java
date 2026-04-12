package com.specgen.core;

import com.specgen.core.model.OpenApiSpec;
import com.specgen.core.utils.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PropertyConfigTest {

    @TempDir
    Path tempDir;

    @Test
    public void testCustomTemplateProperty() throws Exception {
        // 1. Crear un archivo de propiedades con una clave personalizada
        Path propsPath = tempDir.resolve("config.properties");
        String propsContent = "custom.api.version=2.5.0-BETA\ncustom.author=Junie";
        Files.writeString(propsPath, propsContent);

        // 2. Cargar propiedades
        Properties props = FileUtils.loadProperties(propsPath.toString());
        Map<String, Object> additionalProperties = new java.util.HashMap<>();
        props.forEach((k, v) -> additionalProperties.put(k.toString(), v));

        // 3. Crear generador con propiedades adicionales
        OpenApiGenerator generator = new OpenApiGenerator(additionalProperties);

        // 4. Modificar ligeramente la plantilla main.ftl (o simplemente usar una que acceda a las props si las tuviéramos)
        // Como no quiero modificar las plantillas base todavía, voy a verificar que el generador las inyecta en el contexto.
        // Pero para probarlo de verdad, necesito que la plantilla las use.
        // Vamos a crear una plantilla externa temporal.
        
        Path templatesDir = tempDir.resolve("templates");
        Files.createDirectories(templatesDir);
        
        String mainFtl = "openapi: 3.0.3\n" +
                         "info:\n" +
                         "  title: ${info}\n" +
                         "  version: ${.vars['custom.api.version']!'1.0.0'}\n" +
                         "  contact:\n" +
                         "    name: ${.vars['custom.author']!'Unknown'}\n" +
                         "paths: {}\n" +
                         "components:\n" +
                         "  schemas: {}";
        
        Files.writeString(templatesDir.resolve("main.ftl"), mainFtl);
        
        // Añadir la ruta de plantillas a las propiedades
        additionalProperties.put("templates.path", templatesDir.toString());
        
        OpenApiGenerator extGenerator = new OpenApiGenerator(additionalProperties);
        OpenApiSpec spec = new OpenApiSpec("API con Props", List.of());
        
        String result = extGenerator.generate(spec);
        
        System.out.println("[DEBUG_LOG] Result with custom properties and templates:\n" + result);
        
        assertTrue(result.contains("version: 2.5.0-BETA"));
        assertTrue(result.contains("name: Junie"));
        assertTrue(result.contains("title: API con Props"));
    }
}
