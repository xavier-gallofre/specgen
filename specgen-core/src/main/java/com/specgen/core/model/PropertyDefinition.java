package com.specgen.core.model;

/**
 * Definición de una propiedad dentro de un modelo.
 */
public record PropertyDefinition(
    String type,
    String description,
    Integer maxLength,
    Boolean required
) {}
