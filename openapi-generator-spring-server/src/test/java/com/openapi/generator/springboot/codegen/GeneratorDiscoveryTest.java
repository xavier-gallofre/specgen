package com.openapi.generator.springboot.codegen;

import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenConfig;

import java.util.ServiceLoader;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GeneratorDiscoveryTest {

    @Test
    public void testGeneratorIsDiscovered() {
        ServiceLoader<CodegenConfig> loader = ServiceLoader.load(CodegenConfig.class);
        boolean found = false;
        for (CodegenConfig config : loader) {
            if ("springboot-custom".equals(config.getName())) {
                found = true;
                break;
            }
        }
        assertTrue(found, "El generador 'springboot-custom' debería ser descubierto por ServiceLoader");
    }
}
