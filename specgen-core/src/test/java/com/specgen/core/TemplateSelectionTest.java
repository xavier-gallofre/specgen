package com.specgen.core;

import com.specgen.core.model.ModelDefinition;
import com.specgen.core.model.OpenApiSpec;
import com.specgen.core.model.PropertyDefinition;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TemplateSelectionTest {

    @Test
    public void testPropertyTemplateSelection() throws Exception {
        Map<String, PropertyDefinition> properties = new LinkedHashMap<>();
        properties.put("name", new PropertyDefinition("string", "Name", 100, true));
        properties.put("age", new PropertyDefinition("integer", "Age", null, false));
        properties.put("price", new PropertyDefinition("number", "Price", null, false));
        properties.put("active", new PropertyDefinition("boolean", "Active", null, true));
        properties.put("birthdate", new PropertyDefinition("date", "Birth Date", null, false));
        properties.put("other", new PropertyDefinition("unknown", "Other", null, false));

        ModelDefinition model = new ModelDefinition("User", properties, List.of("CRUD"));
        OpenApiSpec spec = new OpenApiSpec("Test API", List.of(model));

        OpenApiGenerator generator = new OpenApiGenerator();
        String result = generator.generateFragment(spec, "User", "schemas.ftl");

        System.out.println("[DEBUG_LOG] Generated Schemas:\n" + result);

        // Verify string template usage (has maxLength)
        assertTrue(result.contains("name:"), "Should contain name property");
        assertTrue(result.contains("type: string"), "Should have type string for name");
        assertTrue(result.contains("maxLength: 100"), "Should have maxLength for name");

        // Verify number template usage
        assertTrue(result.contains("age:"), "Should contain age property");
        assertTrue(result.contains("type: integer"), "Should have type integer for age");

        assertTrue(result.contains("price:"), "Should contain price property");
        assertTrue(result.contains("type: number"), "Should have type number for price");

        // Verify boolean template usage
        assertTrue(result.contains("active:"), "Should contain active property");
        assertTrue(result.contains("type: boolean"), "Should have type boolean for active");

        // Verify date template usage
        assertTrue(result.contains("birthdate:"), "Should contain birthdate property");
        assertTrue(result.contains("format: date"), "Should have format: date for birthdate");

        // Verify generic template usage (for 'unknown' type)
        assertTrue(result.contains("other:"), "Should contain other property");
        assertTrue(result.contains("type: unknown"), "Should have type unknown for other");
        
        // Verify nullable (from my new templates)
        assertTrue(result.contains("nullable: false"), "Should have nullable: false for required properties");
        assertTrue(result.contains("nullable: true"), "Should have nullable: true for optional properties");
    }
}
