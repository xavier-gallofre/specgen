package com.openapi.generator.core.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.openapi.generator.core.model.ModelDefinition;
import com.openapi.generator.core.model.OpenApiSpec;

import java.io.IOException;

/**
 * Utilidad para serializar y deserializar el pseudoformato a YAML.
 */
public class YamlSerializer {

    private final ObjectMapper mapper;

    public YamlSerializer() {
        this.mapper = new ObjectMapper(new YAMLFactory()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
        );
        // Soporte para Records, Optional, Java Time y supresión de nulos
        this.mapper.registerModule(new Jdk8Module());
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Serializa un objeto (OpenApiSpec o ModelDefinition) a una cadena YAML.
     */
    public String serialize(Object obj) throws IOException {
        return mapper.writeValueAsString(obj);
    }

    /**
     * Deserializa una cadena YAML a un ModelDefinition.
     */
    public ModelDefinition deserializeModel(String yaml) throws IOException {
        return mapper.readValue(yaml, ModelDefinition.class);
    }

    /**
     * Deserializa una cadena YAML a un OpenApiSpec.
     */
    public OpenApiSpec deserialize(String yaml) throws IOException {
        return mapper.readValue(yaml, OpenApiSpec.class);
    }
}
