package com.specgen.showcase;

import org.openapitools.codegen.ClientOptInput;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.config.CodegenConfigurator;

import java.io.File;

/**
 * Aplicación de ejemplo que utiliza el generador personalizado 'springboot-custom'
 * para generar un servidor Spring Boot a partir de una especificación OpenAPI.
 */
public class CustomSpringServerApp {

    public static void main(String[] args) {
        String inputSpec = args.length > 0 ? args[0] : "specgen-showcase/generated/openapi/showcase-openapi.yaml";
        String outputDir = args.length > 1 ? args[1] : "specgen-showcase/generated/spring-server";

        File specFile = new File(inputSpec);
        if (!specFile.exists()) {
            System.err.println("No se encuentra el archivo OpenAPI: " + inputSpec);
            System.err.println("Asegúrate de ejecutar ShowcaseApp primero.");
            return;
        }

        System.out.println("Generando servidor Spring Boot personalizado desde: " + inputSpec);
        System.out.println("Carpeta de salida: " + outputDir);

        CodegenConfigurator configurator = new CodegenConfigurator()
                .setGeneratorName("springboot-custom")
                .setInputSpec(inputSpec)
                .setOutputDir(outputDir)
                .setApiPackage("com.example.api")
                .setModelPackage("com.example.model")
                .addAdditionalProperty("interfaceOnly", "true")
                .addAdditionalProperty("useTags", "true");

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        DefaultGenerator generator = new DefaultGenerator();
        generator.opts(clientOptInput).generate();

        System.out.println("Generación completada con éxito en " + outputDir);
    }
}
