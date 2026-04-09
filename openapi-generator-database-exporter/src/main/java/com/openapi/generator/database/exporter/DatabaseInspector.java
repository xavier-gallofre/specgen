package com.openapi.generator.database.exporter;

import com.openapi.generator.core.model.ModelDefinition;
import com.openapi.generator.core.model.OpenApiSpec;
import com.openapi.generator.core.model.PropertyDefinition;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;
import org.hibernate.service.ServiceRegistry;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Servicio encargado de inspeccionar la base de datos y generar un OpenApiSpec.
 */
public class DatabaseInspector {

    /**
     * Genera un OpenApiSpec a partir de una lista de tablas utilizando Hibernate para la gestión de la conexión
     * y metadatos, garantizando independencia de la base de datos.
     */
    public OpenApiSpec exportFromTables(Map<String, Object> settings, List<String> tableNames) {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySettings(settings)
                .build();

        try {
            List<ModelDefinition> models = new ArrayList<>();
            JdbcEnvironment jdbcEnvironment = registry.getService(JdbcEnvironment.class);
            
            // Hibernate 6 utiliza JdbcEnvironment para abstraer detalles del dialecto
            Connection connection = registry.getService(org.hibernate.engine.jdbc.connections.spi.ConnectionProvider.class)
                    .getConnection();

            try {
                DatabaseMetaData metaData = connection.getMetaData();
                for (String tableName : tableNames) {
                    models.add(inspectTable(metaData, tableName, jdbcEnvironment));
                }
            } finally {
                registry.getService(org.hibernate.engine.jdbc.connections.spi.ConnectionProvider.class)
                        .closeConnection(connection);
            }

            return new OpenApiSpec("Generado desde Base de Datos via Hibernate", models);
        } catch (SQLException e) {
            throw new RuntimeException("Error al inspeccionar la base de datos", e);
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    private ModelDefinition inspectTable(DatabaseMetaData metaData, String tableName, JdbcEnvironment jdbcEnvironment) throws SQLException {
        Map<String, PropertyDefinition> properties = new LinkedHashMap<>();
        
        // Ajustamos el nombre de la tabla según el dialecto si es necesario
        String catalog = null;
        String schema = null;
        String name = tableName;
        
        // Algunos JDBC drivers requieren el nombre exacto (mayúsculas/minúsculas)
        // Intentamos buscar la tabla para obtener sus metadatos precisos
        // En H2, las tablas de sistema están en INFORMATION_SCHEMA. Buscamos primero en el esquema actual/PUBLIC.
        try (ResultSet tables = metaData.getTables(null, null, tableName.toUpperCase(), new String[]{"TABLE"})) {
            while (tables.next()) {
                String foundName = tables.getString("TABLE_NAME");
                String foundSchema = tables.getString("TABLE_SCHEM");
                String foundCatalog = tables.getString("TABLE_CAT");
                
                // Prioridad a esquemas que no sean de sistema
                if (name.equals(tableName) || !"INFORMATION_SCHEMA".equals(foundSchema)) {
                    name = foundName;
                    schema = foundSchema;
                    catalog = foundCatalog;
                    if (!"INFORMATION_SCHEMA".equals(foundSchema)) {
                        break; // Encontrada tabla de usuario, paramos
                    }
                }
            }
        }

        if (schema == null) {
            // Reintento con minúsculas si no se encontró nada
            try (ResultSet tablesLower = metaData.getTables(null, null, tableName.toLowerCase(), new String[]{"TABLE"})) {
                if (tablesLower.next()) {
                    name = tablesLower.getString("TABLE_NAME");
                    schema = tablesLower.getString("TABLE_SCHEM");
                    catalog = tablesLower.getString("TABLE_CAT");
                }
            }
        }

        // Búsqueda de columnas iterativa para mayor resiliencia
        boolean columnsFound = false;
        if (name != null) {
            columnsFound = tryFetchColumns(metaData, catalog, schema, name, properties);
        }
        
        if (!columnsFound) {
            // Reintento agresivo para Oracle/H2 si el anterior falló o name era null
            columnsFound = tryFetchColumns(metaData, null, null, tableName.toUpperCase(), properties);
        }
        
        if (!columnsFound && properties.isEmpty()) {
            tryFetchColumns(metaData, null, null, tableName, properties);
        }
        
        return new ModelDefinition(tableName, properties, List.of("CRUD"));
    }

    private boolean tryFetchColumns(DatabaseMetaData metaData, String catalog, String schema, String name, Map<String, PropertyDefinition> properties) throws SQLException {
        try (ResultSet columns = metaData.getColumns(catalog, schema, name, null)) {
            boolean found = false;
            while (columns.next()) {
                found = true;
                String columnName = columns.getString("COLUMN_NAME");
                String typeName = columns.getString("TYPE_NAME");
                int columnSize = columns.getInt("COLUMN_SIZE");
                int nullable = columns.getInt("NULLABLE");

                properties.put(columnName, new PropertyDefinition(
                    mapSqlTypeToOpenApi(typeName),
                    "Columna " + columnName + " de tipo " + typeName,
                    columnSize > 0 ? columnSize : null,
                    nullable == DatabaseMetaData.columnNoNulls
                ));
            }
            return found;
        }
    }

    /**
     * Versión que utiliza una conexión JDBC directa pero con el mapeo mejorado.
     * Mantenida por compatibilidad si es necesario, pero se recomienda usar exportFromTables(Map, List).
     */
    public OpenApiSpec exportFromTables(Connection connection, List<String> tableNames) throws SQLException {
        List<ModelDefinition> models = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();

        for (String tableName : tableNames) {
            models.add(inspectTable(metaData, tableName, null));
        }

        return new OpenApiSpec("Generado desde Base de Datos", models);
    }

    private String mapSqlTypeToOpenApi(String sqlType) {
        return switch (sqlType.toUpperCase()) {
            case "VARCHAR", "CHAR", "TEXT", "CHARACTER", "VARCHAR2", "NVARCHAR2", "CLOB" -> "string";
            case "INTEGER", "INT", "SMALLINT", "TINYINT", "BIGINT" -> "integer";
            case "DECIMAL", "NUMERIC", "DOUBLE", "FLOAT", "REAL", "NUMBER" -> "number";
            case "BOOLEAN", "BIT" -> "boolean";
            case "DATE", "TIMESTAMP", "TIME", "TIMESTAMP WITH TIME ZONE" -> "string";
            default -> "string";
        };
    }
}
