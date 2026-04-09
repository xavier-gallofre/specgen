package com.openapi.generator.core.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Utilidades para la escritura de ficheros de texto.
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
}
