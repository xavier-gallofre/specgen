package com.openapi.generator.core.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

/**
 * Utilidades para la escritura de ficheros de texto y manejo de propiedades.
 */
public class FileUtils {

    /**
     * Escribe el contenido en el fichero especificado.
     * Crea los directorios padre si no existen.
     */
    public static void writeToFile(String filePath, String content) throws IOException {
        Path path = Path.of(filePath);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Carga un archivo de propiedades desde el sistema de archivos o el classpath.
     */
    public static Properties loadProperties(String path) throws IOException {
        Properties props = new Properties();
        Path filePath = Path.of(path);
        if (Files.exists(filePath)) {
            try (FileInputStream fis = new FileInputStream(path)) {
                props.load(fis);
            }
        } else {
            // Intentar cargar desde el classpath si no existe en el FS
            try (var is = FileUtils.class.getClassLoader().getResourceAsStream(path)) {
                if (is != null) {
                    props.load(is);
                } else {
                    throw new IOException("No se pudo encontrar el archivo de propiedades: " + path);
                }
            }
        }
        return props;
    }
    /**
     * Carga el contenido de un recurso desde el classpath.
     */
    public static String readResource(String resourceName) throws IOException {
        try (var is = FileUtils.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new IOException("No se pudo encontrar el recurso: " + resourceName);
            }
            return new String(is.readAllBytes());
        }
    }
}
