package com.specgen.database.exporter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestiona diccionarios de traducción de nombres de tablas y columnas a partir de ficheros CSV.
 */
public class NameDictionary {

    private final Map<String, String> tableDictionary = new HashMap<>();
    private final Map<String, String> columnDictionary = new HashMap<>();
    private final Map<String, String> generalDictionary = new HashMap<>();

    public void loadTableDictionary(String filePath) throws IOException {
        loadCsvToMap(filePath, tableDictionary);
    }

    public void loadColumnDictionary(String filePath) throws IOException {
        loadCsvToMap(filePath, columnDictionary);
    }

    public void loadGeneralDictionary(String filePath) throws IOException {
        loadCsvToMap(filePath, generalDictionary);
    }

    private void loadCsvToMap(String filePath, Map<String, String> targetMap) throws IOException {
        if (filePath == null) return;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    targetMap.put(parts[0].trim(), parts[1].trim());
                }
            }
        }
    }

    /**
     * Resuelve el nombre de una tabla siguiendo la prioridad: Tabla > General.
     */
    public String resolveTableName(String originalName) {
        if (tableDictionary.containsKey(originalName)) {
            return tableDictionary.get(originalName);
        }
        return generalDictionary.getOrDefault(originalName, originalName);
    }

    /**
     * Resuelve el nombre de una columna siguiendo la prioridad: 
     * Columna específica (Tabla.Columna) > Columna general > General.
     */
    public String resolveColumnName(String tableName, String columnName) {
        String specificKey = tableName + "." + columnName;
        if (columnDictionary.containsKey(specificKey)) {
            return columnDictionary.get(specificKey);
        }
        if (columnDictionary.containsKey(columnName)) {
            return columnDictionary.get(columnName);
        }
        return generalDictionary.getOrDefault(columnName, columnName);
    }
}
