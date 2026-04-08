package com.openapi.generator.database.exporter;

import com.openapi.generator.core.model.ModelDefinition;
import com.openapi.generator.core.model.OpenApiSpec;
import com.openapi.generator.core.model.PropertyDefinition;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;

import java.util.*;

/**
 * Servicio encargado de inspeccionar la base de datos y generar un OpenApiSpec.
 */
public class DatabaseInspector {

    /**
     * Inspecciona una tabla específica y devuelve su definición de modelo.
     * En una implementación real, esto podría usar JDBC DatabaseMetaData o Hibernate Metadata.
     */
    public ModelDefinition inspectTable(String tableName, StandardServiceRegistry registry) {
        MetadataSources sources = new MetadataSources(registry);
        Metadata metadata = sources.buildMetadata();

        // En Hibernate 6, obtener tablas no es tan directo como antes para ingeniería inversa manual sin entidades.
        // Pero para este ejemplo, simularemos la obtención de metadatos de la tabla.
        
        Map<String, PropertyDefinition> properties = new LinkedHashMap<>();
        
        // Simulación de inspección de columnas (se podría usar JDBC directamente)
        // Por ahora, devolvemos un esquema base.
        
        return new ModelDefinition(
            tableName,
            properties,
            List.of("CRUD") // Por defecto generamos CRUD
        );
    }

    /**
     * Genera un OpenApiSpec a partir de una lista de tablas usando JDBC directo para mayor control.
     */
    public OpenApiSpec exportFromTables(java.sql.Connection connection, List<String> tableNames) throws java.sql.SQLException {
        List<ModelDefinition> models = new ArrayList<>();
        java.sql.DatabaseMetaData metaData = connection.getMetaData();

        for (String tableName : tableNames) {
            Map<String, PropertyDefinition> properties = new LinkedHashMap<>();
            try (java.sql.ResultSet columns = metaData.getColumns(null, null, tableName, null)) {
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String typeName = columns.getString("TYPE_NAME");
                    int columnSize = columns.getInt("COLUMN_SIZE");
                    int nullable = columns.getInt("NULLABLE");

                    properties.put(columnName, new PropertyDefinition(
                        mapSqlTypeToOpenApi(typeName),
                        "Columna " + columnName + " de tipo " + typeName,
                        columnSize > 0 ? columnSize : null,
                        nullable == java.sql.DatabaseMetaData.columnNoNulls
                    ));
                }
            }
            models.add(new ModelDefinition(tableName, properties, List.of("CRUD")));
        }

        return new OpenApiSpec("Generado desde Base de Datos", models);
    }

    private String mapSqlTypeToOpenApi(String sqlType) {
        return switch (sqlType.toUpperCase()) {
            case "VARCHAR", "CHAR", "TEXT", "CHARACTER" -> "string";
            case "INTEGER", "INT", "SMALLINT", "TINYINT", "BIGINT" -> "integer";
            case "DECIMAL", "NUMERIC", "DOUBLE", "FLOAT", "REAL" -> "number";
            case "BOOLEAN", "BIT" -> "boolean";
            case "DATE", "TIMESTAMP", "TIME" -> "string"; // Podría ser más específico con format
            default -> "string";
        };
    }
}
