package com.specgen.core.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Representa una zona de trabajo (workspace) que organiza plantillas, diccionarios y propiedades.
 */
public class Workspace {

    private final Path rootPath;
    private final Path templatesPath;
    private final Path dictionaryPath;
    private final Path generatedPath;
    private final Properties properties;

    public Workspace(String path) throws IOException {
        this.rootPath = Path.of(path).toAbsolutePath();
        this.templatesPath = rootPath.resolve("templates");
        this.dictionaryPath = rootPath.resolve("dictionary");
        this.generatedPath = rootPath.resolve("generated");
        
        Path propsFile = rootPath.resolve("workspace.properties");
        if (Files.exists(propsFile)) {
            this.properties = FileUtils.loadProperties(propsFile.toString());
        } else {
            this.properties = new Properties();
        }
    }

    public Path getRootPath() {
        return rootPath;
    }

    public Path getTemplatesPath() {
        return templatesPath;
    }

    public Path getDictionaryPath() {
        return dictionaryPath;
    }

    public Path getGeneratedPath() {
        return generatedPath;
    }

    public Properties getProperties() {
        return properties;
    }

    public boolean hasTemplates() {
        return Files.exists(templatesPath) && Files.isDirectory(templatesPath);
    }

    public boolean hasDictionary() {
        return Files.exists(dictionaryPath) && Files.isDirectory(dictionaryPath);
    }
}
