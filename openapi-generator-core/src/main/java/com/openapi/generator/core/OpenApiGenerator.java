package com.openapi.generator.core;

import com.openapi.generator.core.model.OpenApiSpec;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

public class OpenApiGenerator {

    private final Configuration cfg;

    public OpenApiGenerator() {
        this.cfg = new Configuration(Configuration.VERSION_2_3_32);
        this.cfg.setClassForTemplateLoading(OpenApiGenerator.class, "/templates");
        this.cfg.setDefaultEncoding("UTF-8");
        this.cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        this.cfg.setLogTemplateExceptions(false);
        this.cfg.setWrapUncheckedExceptions(true);
        this.cfg.setFallbackOnNullLoopVariable(false);

        // Permitir acceso directo a campos de Records en las plantillas
        this.cfg.setAPIBuiltinEnabled(true);
    }

    public String generate(OpenApiSpec spec) throws IOException, TemplateException {
        Template temp = cfg.getTemplate("main.ftl");
        StringWriter out = new StringWriter();
        
        // Pasamos los modelos directamente al contexto de la plantilla
        Map<String, Object> root = Map.of(
            "models", spec.models(),
            "info", spec.info() != null ? spec.info() : "API Generada"
        );
        
        temp.process(root, out);
        return out.toString();
    }
}
