package com.openapi.generator.core.utils;

import com.openapi.generator.core.model.ModelDefinition;
import com.openapi.generator.core.model.OpenApiSpec;
import com.openapi.generator.core.model.PropertyDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class YamlSerializerTest {

    @Test
    public void testSerializationAndDeserialization() throws Exception {
        YamlSerializer serializer = new YamlSerializer();

        PropertyDefinition id = new PropertyDefinition("integer", "ID único", null, true);
        PropertyDefinition name = new PropertyDefinition("string", "Nombre", 100, true);

        ModelDefinition user = new ModelDefinition("User", Map.of("id", id, "name", name), List.of("CRUD"));
        OpenApiSpec spec = new OpenApiSpec("API Test", List.of(user));

        // 1. Serializar
        String yaml = serializer.serialize(spec);
        System.out.println("[DEBUG_LOG] Serialized YAML:\n" + yaml);

        assertNotNull(yaml);
        assertTrue(yaml.contains("info: API Test"));
        assertTrue(yaml.contains("name: User"));

        // 2. Deserializar
        OpenApiSpec deserialized = serializer.deserialize(yaml);

        assertEquals(spec.info(), deserialized.info());
        assertEquals(spec.models().size(), deserialized.models().size());
        assertEquals("User", deserialized.models().get(0).name());
        
        ModelDefinition deserializedUser = deserialized.models().get(0);
        assertTrue(deserializedUser.properties().containsKey("id"));
        assertEquals("integer", deserializedUser.properties().get("id").type());
    }

    @Test
    public void testManualYamlToSpec() throws Exception {
        String yaml = """
                info: Manual API
                models:
                - name: Product
                  properties:
                    id:
                      type: integer
                      description: Product ID
                      required: true
                    price:
                      type: number
                  generate:
                  - CRUD
                """;

        YamlSerializer serializer = new YamlSerializer();
        OpenApiSpec spec = serializer.deserialize(yaml);

        assertEquals("Manual API", spec.info());
        assertEquals(1, spec.models().size());
        ModelDefinition product = spec.models().get(0);
        assertEquals("Product", product.name());
        assertEquals("integer", product.properties().get("id").type());
        assertTrue(product.properties().get("id").required());
        assertNull(product.properties().get("price").description());
    }
}
