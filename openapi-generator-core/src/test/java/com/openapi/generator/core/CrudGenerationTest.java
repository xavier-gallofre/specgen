package com.openapi.generator.core;

import com.openapi.generator.core.model.ModelDefinition;
import com.openapi.generator.core.model.OpenApiSpec;
import com.openapi.generator.core.model.PropertyDefinition;
import com.openapi.generator.core.utils.YamlSerializer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CrudGenerationTest {

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
        assertTrue(result.contains("User:"));
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
        assertTrue(result.contains("Customer:"));
    }
}
