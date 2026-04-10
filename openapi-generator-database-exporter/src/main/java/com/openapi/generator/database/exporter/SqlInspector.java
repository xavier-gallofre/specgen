package com.openapi.generator.database.exporter;

import com.openapi.generator.core.model.OpenApiSpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

/**
 * Servicio que permite generar un OpenApiSpec a partir de sentencias DDL SQL.
 * Utiliza una base de datos H2 en memoria temporal para procesar el SQL y extraer metadatos.
 */
public class SqlInspector {

    private final DatabaseInspector databaseInspector;

    public SqlInspector() {
        this.databaseInspector = new DatabaseInspector();
    }

    /**
     * Genera un OpenApiSpec a partir de un script SQL DDL.
     * 
     * @param ddl El contenido del script SQL (CREATE TABLE, etc.)
     * @param tableNames Lista de tablas a extraer del DDL procesado.
     * @return OpenApiSpec con la definición de las tablas.
     */
    public OpenApiSpec exportFromSql(String ddl, List<String> tableNames) {
        // Usamos un nombre de BD único para evitar colisiones si se ejecuta en paralelo
        String dbName = "sql_inspector_" + UUID.randomUUID().toString().replace("-", "");
        String url = "jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1;MODE=Oracle";

        try (Connection conn = DriverManager.getConnection(url)) {
            // 1. Ejecutar el DDL en la base de datos temporal
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(ddl);
            }

            // 2. Utilizar el DatabaseInspector existente para extraer la información
            OpenApiSpec spec = databaseInspector.exportFromTables(conn, tableNames);
            
            // Ajustar la descripción de la info
            return new OpenApiSpec("Generado desde DDL SQL", spec.models());

        } catch (SQLException e) {
            throw new RuntimeException("Error al procesar el DDL SQL", e);
        } finally {
            // Cerrar la base de datos H2 en memoria explícitamente
            try (Connection conn = DriverManager.getConnection(url + ";DROP=TRUE")) {
                // La conexión se cierra sola, y DROP=TRUE (si se soporta vía URL en algunas versiones) 
                // o simplemente dejar que se limpie al cerrar todas las conexiones si no hay DB_CLOSE_DELAY.
                // Como pusimos DB_CLOSE_DELAY=-1, forzamos el SHUTDOWN.
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("SHUTDOWN");
                }
            } catch (SQLException e) {
                // Ignorar error al cerrar
            }
        }
    }
}
