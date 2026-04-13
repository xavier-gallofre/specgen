package com.specgen.database.exporter;

import com.specgen.core.model.PropertyDefinition;

/**
 * Regla que transforma columnas VARCHAR(1) o CHAR(1) en booleanos.
 * Útil para campos tipo 'S/N', '1/0', 'Y/N'.
 */
public class VarcharToBooleanRule implements ExportRule {

    @Override
    public PropertyDefinition apply(String tableName, String columnName, String sqlType, Integer columnSize, PropertyDefinition currentProp) {
        if (sqlType == null) return currentProp;
        String upperType = sqlType.toUpperCase();
        if ((upperType.contains("VARCHAR") || upperType.contains("CHAR")) 
            && columnSize != null && columnSize == 1) {
            
            return new PropertyDefinition(
                "boolean",
                currentProp.description() + " (Transformado de " + sqlType + "(1) a boolean)",
                null,
                currentProp.required()
            );
        }
        return currentProp;
    }

    @Override
    public String getReviewNote(String tableName, String columnName, String sqlType, Integer columnSize) {
        if (sqlType == null) return null;
        String upperType = sqlType.toUpperCase();
        if ((upperType.contains("VARCHAR") || upperType.contains("CHAR")) 
            && columnSize != null && columnSize == 1) {
            return "Columna " + sqlType + "(1) detectada. Se ha transformado a boolean. " +
                   "Asegúrese de que en base de datos solo contenga valores booleanos (ej: S/N, 1/0, Y/N).";
        }
        return null;
    }
}
