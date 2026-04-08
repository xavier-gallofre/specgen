package com.openapi.generator.core.model;

import java.util.List;
import java.util.Map;

/**
 * Definición de un modelo (objeto de negocio).
 */
public record ModelDefinition(
    String name,
    Map<String, PropertyDefinition> properties,
    List<String> generate // Ej: ["CRUD", "SELECTOR"]
) {}
