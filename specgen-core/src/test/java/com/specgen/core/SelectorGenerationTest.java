package com.specgen.core;

import com.specgen.core.model.ModelDefinition;
import com.specgen.core.model.OpenApiSpec;
import com.specgen.core.model.PropertyDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SelectorGenerationTest {

    @Test
    public void testGenerateSelector() throws Exception {
        OpenApiGenerator generator = new OpenApiGenerator();

        PropertyDefinition id = new PropertyDefinition("integer", "ID", null, true);
        PropertyDefinition name = new PropertyDefinition("string", "Nombre", null, true);

        ModelDefinition productModel = new ModelDefinition(
            "Product",
            Map.of("id", id, "name", name),
            List.of("SELECTOR")
        );

        OpenApiSpec spec = new OpenApiSpec("API Selector", List.of(productModel));

        String result = generator.generate(spec);

        System.out.println("[DEBUG_LOG] Output:\n" + result);

        // Verificaciones
        assertTrue(result.contains("/products/selector:"));
        assertTrue(result.contains("label:"));
        assertTrue(result.contains("Identificador único"));
        assertTrue(result.contains("type: array"));
    }
}
