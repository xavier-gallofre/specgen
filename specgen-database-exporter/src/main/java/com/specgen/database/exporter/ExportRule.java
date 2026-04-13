package com.specgen.database.exporter;

import com.specgen.core.model.PropertyDefinition;

/**
 * Interfaz para definir reglas de transformación personalizadas durante la exportación.
 */
public interface ExportRule {
    /**
     * Aplica la regla a una definición de propiedad.
     * 
     * @param tableName Nombre de la tabla (original).
     * @param columnName Nombre de la columna (original).
     * @param sqlType Tipo SQL de la columna (ej: VARCHAR, NUMBER).
     * @param columnSize Tamaño de la columna.
     * @param currentProp Definición de propiedad actual.
     * @return La nueva definición de propiedad (puede ser la misma si no hay cambios) o null si se quiere omitir.
     */
    PropertyDefinition apply(String tableName, String columnName, String sqlType, Integer columnSize, PropertyDefinition currentProp);

    /**
     * Devuelve una descripción de por qué se aplicó la regla, para el informe de revisión.
     * Si devuelve null, no se registra en el informe.
     */
    String getReviewNote(String tableName, String columnName, String sqlType, Integer columnSize);
}
