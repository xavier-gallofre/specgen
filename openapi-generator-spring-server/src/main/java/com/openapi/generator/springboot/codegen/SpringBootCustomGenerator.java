package com.openapi.generator.springboot.codegen;

import org.openapitools.codegen.languages.SpringCodegen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generador personalizado para Spring Boot que extiende el generador oficial 'spring'.
 * Permite añadir personalizaciones específicas y usar plantillas propias.
 */
public class SpringBootCustomGenerator extends SpringCodegen {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringBootCustomGenerator.class);

    public SpringBootCustomGenerator() {
        super();
        
        // Indicamos dónde estarán nuestras plantillas personalizadas dentro de resources
        this.templateDir = "springboot-custom";
        this.embeddedTemplateDir = "springboot-custom";
    }

    @Override
    public String getName() {
        return "springboot-custom";
    }

    @Override
    public String getHelp() {
        return "Generador personalizado basado en Spring Boot para el proyecto xganie.";
    }

    @Override
    public void processOpts() {
        super.processOpts();
        
        // Sobrescribimos el directorio de plantillas de nuevo tras processOpts de la clase base
        this.templateDir = "springboot-custom";
        this.embeddedTemplateDir = "springboot-custom";

        // Limpiamos los archivos de soporte por defecto para simplificar
        supportingFiles.clear();

        // Añadimos archivos adicionales por cada modelo (Entity, Repository, Service)
        // La clase base SpringCodegen ya genera los modelos usando model.mustache -> pojo.mustache
        
        // Para añadir archivos adicionales por modelo, podemos usar modelTemplateFiles
        modelTemplateFiles.put("repository.mustache", "Repository.java");
        modelTemplateFiles.put("service.mustache", "Service.java");

        // IMPORTANTE: Asegurarnos de que el generador use nuestra plantilla pojo.mustache
        // A veces SpringCodegen fuerza sus propias plantillas.
        modelDocTemplateFiles.remove("model_doc.mustache");
        modelTestTemplateFiles.remove("model_test.mustache");
    }

    @Override
    public void postProcessModelProperty(org.openapitools.codegen.CodegenModel model, org.openapitools.codegen.CodegenProperty property) {
        super.postProcessModelProperty(model, property);
        // Podemos añadir lógica aquí si necesitamos marcar el ID para JPA
        if ("id".equalsIgnoreCase(property.name)) {
            property.vendorExtensions.put("x-is-id", true);
        }
    }
}
