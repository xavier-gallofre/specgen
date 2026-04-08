package com.openapi.generator.core.model;

import java.util.List;

/**
 * Representa el pseudo-formato intermedio para la generación de OpenAPI.
 */
public record OpenApiSpec(
    String info,
    List<ModelDefinition> models
) {}
