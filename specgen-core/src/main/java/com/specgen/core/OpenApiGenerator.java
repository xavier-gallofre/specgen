package com.specgen.core;

import com.specgen.core.model.OpenApiSpec;
import com.specgen.core.utils.FileUtils;
import com.specgen.core.utils.TextUtils;
import com.specgen.core.utils.Workspace;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class OpenApiGenerator {

    private final Configuration cfg;
    private final Map<String, Object> additionalProperties;
    private Workspace workspace;

    public OpenApiGenerator() {
        this(new HashMap<>());
    }

    public OpenApiGenerator(Map<String, Object> additionalProperties) {
        this.cfg = new Configuration(Configuration.VERSION_2_3_32);
        this.cfg.setClassForTemplateLoading(OpenApiGenerator.class, "/templates");
        this.cfg.setDefaultEncoding("UTF-8");
        this.cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        this.cfg.setLogTemplateExceptions(false);
        this.cfg.setWrapUncheckedExceptions(true);
        this.cfg.setFallbackOnNullLoopVariable(false);

        // Permitir acceso directo a campos de Records en las plantillas
        this.cfg.setAPIBuiltinEnabled(true);
        this.additionalProperties = additionalProperties != null ? additionalProperties : new HashMap<>();
        
        // Añadir utilidades de texto al contexto global
        this.additionalProperties.put("textUtils", new TextUtils());
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
        if (workspace != null && workspace.hasTemplates()) {
            try {
                cfg.setDirectoryForTemplateLoading(workspace.getTemplatesPath().toFile());
            } catch (IOException e) {
                throw new RuntimeException("Error al configurar el directorio de plantillas del workspace", e);
            }
        }
    }

    /**
     * Genera un fragmento de la especificación OpenAPI usando una plantilla específica.
     */
    public String generateFragment(OpenApiSpec spec, String modelName, String templateName) throws IOException, TemplateException {
        // Soporte para configurar el recurso externo de plantillas si se define en las propiedades
        if (workspace != null && workspace.hasTemplates()) {
             cfg.setDirectoryForTemplateLoading(workspace.getTemplatesPath().toFile());
        } else if (additionalProperties.containsKey("templates.path")) {
            String path = (String) additionalProperties.get("templates.path");
            Path templateDirPath = Path.of(path);
            if (Files.exists(templateDirPath) && Files.isDirectory(templateDirPath)) {
                cfg.setDirectoryForTemplateLoading(templateDirPath.toFile());
            }
        } else {
            // Volver a la carga por defecto
            cfg.setClassForTemplateLoading(OpenApiGenerator.class, "/templates");
        }

        var model = spec.models().stream()
                .filter(m -> m.name().equals(modelName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Modelo no encontrado: " + modelName));

        Template temp = cfg.getTemplate(templateName);
        StringWriter out = new StringWriter();

        Map<String, Object> root = new HashMap<>(additionalProperties);
        root.put("model", model);
        // Algunos templates (como schemas.ftl) pueden esperar "models" (lista)
        root.put("models", java.util.List.of(model));

        temp.process(root, out);
        return out.toString();
    }

    /**
     * Genera la especificación OpenAPI para un solo modelo (parcial).
     */
    public String generatePartial(OpenApiSpec spec, String modelName) throws IOException, TemplateException {
        return generateFragment(spec, modelName, "partial.ftl");
    }

    /**
     * Genera la especificación OpenAPI y la devuelve como String.
     */
    public String generate(OpenApiSpec spec) throws IOException, TemplateException {
        // Soporte para configurar el recurso externo de plantillas si se define en las propiedades
        if (workspace != null && workspace.hasTemplates()) {
            cfg.setDirectoryForTemplateLoading(workspace.getTemplatesPath().toFile());
        } else if (additionalProperties.containsKey("templates.path")) {
            String path = (String) additionalProperties.get("templates.path");
            Path templateDirPath = Path.of(path);
            if (Files.exists(templateDirPath) && Files.isDirectory(templateDirPath)) {
                cfg.setDirectoryForTemplateLoading(templateDirPath.toFile());
            }
        } else {
            // Volver a la carga por defecto si no hay workspace ni path explícito
            cfg.setClassForTemplateLoading(OpenApiGenerator.class, "/templates");
        }

        Template temp = cfg.getTemplate("main.ftl");
        StringWriter out = new StringWriter();
        
        // Combinamos modelos con propiedades adicionales
        Map<String, Object> root = new HashMap<>(additionalProperties);
        root.put("models", spec.models());
        root.put("info", spec.info() != null ? spec.info() : "API Generada");
        
        temp.process(root, out);
        return out.toString();
    }

    /**
     * Genera la especificación OpenAPI y la guarda en un archivo.
     */
    public void generateToFile(OpenApiSpec spec, String outputPath) throws IOException, TemplateException {
        String content = generate(spec);
        FileUtils.writeToFile(outputPath, content);
    }
}
