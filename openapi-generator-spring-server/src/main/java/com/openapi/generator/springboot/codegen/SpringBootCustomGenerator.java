package com.openapi.generator.springboot.codegen;

import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.languages.SpringCodegen;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.ModelsMap;
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
    public String modelFileFolder() {
        return outputFolder + java.io.File.separator + sourceFolder + java.io.File.separator + modelPackage.replace('.', java.io.File.separatorChar);
    }

    @Override
    public String modelFilename(String templateName, String modelName) {
        String suffix = modelTemplateFiles().get(templateName);
        if ("Service.java".equals(suffix)) {
            String servicePkg = (String) additionalProperties.get("servicePackage");
            return outputFolder + java.io.File.separator + sourceFolder + java.io.File.separator + servicePkg.replace('.', java.io.File.separatorChar) + java.io.File.separator + modelName + "Service.java";
        } else if ("Repository.java".equals(suffix)) {
            String repoPkg = (String) additionalProperties.get("repositoryPackage");
            return outputFolder + java.io.File.separator + sourceFolder + java.io.File.separator + repoPkg.replace('.', java.io.File.separatorChar) + java.io.File.separator + modelName + "Repository.java";
        }
        
        // Para el modelo normal (pojo.mustache), forzamos que sea el modelName puro
        return super.modelFilename(templateName, modelName);
    }

    @Override
    public String apiFileFolder() {
        return outputFolder + java.io.File.separator + sourceFolder + java.io.File.separator + apiPackage.replace('.', java.io.File.separatorChar);
    }

    @Override
    public void processOpts() {
        // Forzamos los paquetes antes de llamar a super.processOpts()
        if (!additionalProperties.containsKey("modelPackage")) {
            additionalProperties.put("modelPackage", "com.example.domain.model");
        }
        if (!additionalProperties.containsKey("apiPackage")) {
            additionalProperties.put("apiPackage", "com.example.infrastructure.api");
        }
        if (!additionalProperties.containsKey("invokerPackage")) {
            additionalProperties.put("invokerPackage", "com.example");
        }
        if (!additionalProperties.containsKey("basePackage")) {
            additionalProperties.put("basePackage", "com.example");
        }

        super.processOpts();
        
        // Sobrescribimos el directorio de plantillas de nuevo tras processOpts de la clase base
        this.templateDir = "springboot-custom";
        this.embeddedTemplateDir = "springboot-custom";

        // Limpiamos los archivos de soporte por defecto para simplificar
        supportingFiles.clear();

        // Configuración para arquitectura hexagonal (separación por paquetes)
        String basePkg = (String) additionalProperties.get("basePackage");
        
        // Paquetes definidos para la arquitectura hexagonal
        String domainPackage = basePkg + ".domain";
        String infraPackage = basePkg + ".infrastructure";
        
        String modelPkg = domainPackage + ".model";
        String servicePkg = domainPackage + ".service";
        String repositoryPkg = infraPackage + ".persistence";
        String apiPkg = infraPackage + ".api";

        this.setModelPackage(modelPkg);
        this.setApiPackage(apiPkg);
        this.setInvokerPackage(basePkg);
        
        additionalProperties.put("modelPackage", modelPkg);
        additionalProperties.put("servicePackage", servicePkg);
        additionalProperties.put("repositoryPackage", repositoryPkg);
        additionalProperties.put("apiPackage", apiPkg);
        additionalProperties.put("invokerPackage", basePkg);

        // IMPORTANTE: Asegurarnos de que el generador use nuestra plantilla pojo.mustache
        modelDocTemplateFiles.remove("model_doc.mustache");
        modelTestTemplateFiles.remove("model_test.mustache");
        
        // Añadimos las plantillas para servicios y repositorios como archivos de modelo
        // para que se generen por cada modelo encontrado.
        modelTemplateFiles.put("service.mustache", "Service.java");
        modelTemplateFiles.put("repository.mustache", "Repository.java");
    }

    @Override
    public java.util.Map<String, ModelsMap> postProcessAllModels(java.util.Map<String, ModelsMap> objs) {
        java.util.Map<String, ModelsMap> result = super.postProcessAllModels(objs);
        
        java.util.Map<String, CodegenModel> entitiesToGenerate = new java.util.HashMap<>();

        // 1. Identificar DTOs y preparar Entidades base
        for (ModelsMap modelsMap : result.values()) {
            for (ModelMap modelMap : modelsMap.getModels()) {
                CodegenModel cm = modelMap.getModel();
                String baseName = cm.classname.replace("View", "").replace("Form", "");
                
                boolean isDto = cm.classname.endsWith("View") || cm.classname.endsWith("Form");
                cm.vendorExtensions.put("x-is-dto", isDto);
                cm.vendorExtensions.put("x-is-entity", !isDto);
                cm.vendorExtensions.put("x-entity-name", baseName);
                
                // Inyectamos los paquetes en vendorExtensions para que las plantillas tengan acceso
                cm.vendorExtensions.put("x-service-package", additionalProperties.get("servicePackage"));
                cm.vendorExtensions.put("x-repository-package", additionalProperties.get("repositoryPackage"));
                // También en additionalProperties para asegurar disponibilidad global si falla x-
                additionalProperties.put("x-service-package", additionalProperties.get("servicePackage"));
                additionalProperties.put("x-repository-package", additionalProperties.get("repositoryPackage"));
                additionalProperties.put("x-model-package", additionalProperties.get("modelPackage"));

                if (isDto) {
                    if (!entitiesToGenerate.containsKey(baseName)) {
                        CodegenModel entity = new CodegenModel();
                        entity.classname = baseName;
                        entity.name = baseName;
                        entity.classFilename = baseName;
                        entity.description = "Entidad base para " + baseName;
                        entity.vars = new java.util.ArrayList<>();
                        entity.allVars = new java.util.ArrayList<>();
                        entity.vendorExtensions = new java.util.HashMap<>();
                        entity.vendorExtensions.put("x-is-entity", true);
                        entity.vendorExtensions.put("x-is-dto", false);
                        entity.vendorExtensions.put("x-entity-name", baseName);
                        entity.vendorExtensions.put("x-service-package", additionalProperties.get("servicePackage"));
                        entity.vendorExtensions.put("x-repository-package", additionalProperties.get("repositoryPackage"));
                        entitiesToGenerate.put(baseName, entity);
                    }
                    
                    // Aseguramos que la entidad tenga todas las variables únicas de los DTOs
                    CodegenModel entity = entitiesToGenerate.get(baseName);
                    for (org.openapitools.codegen.CodegenProperty var : cm.vars) {
                        boolean exists = entity.vars.stream().anyMatch(v -> v.name.equals(var.name));
                        if (!exists) {
                            entity.vars.add(var);
                            entity.allVars.add(var);
                        }
                    }
                }
            }
        }

        // 2. Inyectar Entidades al flujo de modelos
        for (CodegenModel entity : entitiesToGenerate.values()) {
            if (!result.containsKey(entity.classname)) {
                ModelsMap entityModelsMap = new ModelsMap();
                ModelMap entityModelMap = new ModelMap();
                entityModelMap.setModel(entity);
                entityModelsMap.setModels(java.util.Collections.singletonList(entityModelMap));
                result.put(entity.classname, entityModelsMap);
            }
        }

        return result;
    }

    @Override
    public void postProcessModelProperty(CodegenModel model, org.openapitools.codegen.CodegenProperty property) {
        super.postProcessModelProperty(model, property);
        // Podemos añadir lógica aquí si necesitamos marcar el ID para JPA
        if ("id".equalsIgnoreCase(property.name)) {
            property.vendorExtensions.put("x-is-id", true);
        }
    }
}
