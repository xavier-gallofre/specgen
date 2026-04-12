package com.specgen.core;

import com.specgen.core.model.ModelDefinition;
import com.specgen.core.model.OpenApiSpec;
import com.specgen.core.model.PropertyDefinition;
import com.specgen.core.utils.YamlSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CrudGenerationTest {

    @TempDir
    Path tempDir;

    @Test
    public void testGenerateCrud() throws Exception {
        OpenApiGenerator generator = new OpenApiGenerator();

        PropertyDefinition id = new PropertyDefinition("integer", "ID único", null, true);
        PropertyDefinition name = new PropertyDefinition("string", "Nombre del usuario", 100, true);

        ModelDefinition userModel = new ModelDefinition(
            "User",
            Map.of("id", id, "name", name),
            List.of("CRUD")
        );

        OpenApiSpec spec = new OpenApiSpec("API de Prueba", List.of(userModel));

        String result = generator.generate(spec);

        System.out.println("[DEBUG_LOG] Output:\n" + result);

        // Verificaciones básicas
        assertTrue(result.contains("title: API de Prueba"));
        assertTrue(result.contains("/users:"));
        assertTrue(result.contains("get:"));
        assertTrue(result.contains("post:"));
        assertTrue(result.contains("/users/{id}:"));
        assertTrue(result.contains("UserView:"));
        assertTrue(result.contains("UserForm:"));
        assertTrue(result.contains("maxLength: 100"));
    }

    @Test
    public void testGenerateFromYaml() throws Exception {
        String yaml = """
                info: API desde YAML
                models:
                - name: Customer
                  properties:
                    id:
                      type: integer
                      required: true
                    email:
                      type: string
                  generate:
                  - CRUD
                """;

        YamlSerializer serializer = new YamlSerializer();
        OpenApiSpec spec = serializer.deserialize(yaml);

        OpenApiGenerator generator = new OpenApiGenerator();
        String result = generator.generate(spec);

        System.out.println("[DEBUG_LOG] Output from YAML:\n" + result);

        assertTrue(result.contains("title: API desde YAML"));
        assertTrue(result.contains("/customers:"));
        assertTrue(result.contains("CustomerView:"));
        assertTrue(result.contains("CustomerForm:"));
    }

    @Test
    public void testGenerateToFile() throws Exception {
        String yaml = """
                info: API para Archivo
                models:
                - name: Item
                  properties:
                    id:
                      type: integer
                      required: true
                  generate:
                  - CRUD
                """;

        YamlSerializer serializer = new YamlSerializer();
        OpenApiSpec spec = serializer.deserialize(yaml);

        OpenApiGenerator generator = new OpenApiGenerator();
        Path outputPath = tempDir.resolve("openapi.yaml");
        generator.generateToFile(spec, outputPath.toString());

        assertTrue(Files.exists(outputPath), "OpenAPI file should exist");
        String content = Files.readString(outputPath);
        assertTrue(content.contains("title: API para Archivo"));
        assertTrue(content.contains("ItemView:"));
        assertTrue(content.contains("ItemForm:"));
    }
}
