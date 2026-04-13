package com.specgen.database.exporter;

import com.specgen.core.model.OpenApiSpec;
import com.specgen.core.model.PropertyDefinition;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class ExportRulesTest {

    @Test
    public void testVarcharToBooleanRule() {
        String ddl = """
            CREATE TABLE FLAGS (
                ID NUMBER(10) PRIMARY KEY,
                ACTIVE VARCHAR2(1),
                ENABLED CHAR(1) NOT NULL,
                NAME VARCHAR2(50)
            );
        """;

        ExportRuleManager ruleManager = new ExportRuleManager();
        ruleManager.addRule(new VarcharToBooleanRule());

        SqlInspector inspector = new SqlInspector();
        inspector.setRuleManager(ruleManager);
        
        OpenApiSpec spec = inspector.exportFromSql(ddl, List.of("FLAGS"));
        
        var model = spec.models().get(0);
        PropertyDefinition activeProp = model.properties().get("ACTIVE");
        assertNotNull(activeProp, "La propiedad ACTIVE debería existir. Disponibles: " + model.properties().keySet());
        assertEquals("boolean", activeProp.type(), "Tipo de ACTIVE incorrecto. Desc: " + activeProp.description());
        assertEquals("boolean", model.properties().get("ENABLED").type());
        assertEquals("string", model.properties().get("NAME").type());
        
        assertTrue(model.properties().get("ACTIVE").description().contains("Transformado"));
        
        assertEquals(2, ruleManager.getReviewLog().size());
    }

    @Test
    public void testReviewReportGeneration(@org.junit.jupiter.api.io.TempDir Path tempDir) throws IOException {
        String ddl = "CREATE TABLE TEST ( IS_OK VARCHAR2(1) );";
        
        ExportRuleManager ruleManager = new ExportRuleManager();
        ruleManager.addRule(new VarcharToBooleanRule());

        DatabaseExportFileWriter writer = new DatabaseExportFileWriter();
        writer.setRuleManager(ruleManager);
        
        // Usamos exportSqlToPartialFiles si existiera, pero vamos a usar exportToPartialFiles con H2
        Map<String, Object> dbSettings = Map.of(
            "hibernate.connection.url", "jdbc:h2:mem:test_report;DB_CLOSE_DELAY=-1",
            "hibernate.connection.driver_class", "org.h2.Driver"
        );
        
        // Inicializar la tabla
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection("jdbc:h2:mem:test_report;DB_CLOSE_DELAY=-1")) {
            conn.createStatement().execute(ddl);
        } catch (java.sql.SQLException e) {
            fail(e);
        }

        writer.exportToPartialFiles(dbSettings, List.of("TEST"), tempDir.toString());
        
        Path reportFile = tempDir.resolve("revisar.md");
        assertTrue(Files.exists(reportFile), "El archivo revisar.md debería existir");
        
        String content = Files.readString(reportFile);
        System.out.println("[DEBUG_LOG] Report content:\n" + content);
        assertTrue(content.contains("TEST"));
        assertTrue(content.contains("IS_OK"));
        assertTrue(content.contains("detectada"));
        assertTrue(content.contains("intermediate/partials/TEST.txt"));
    }
}
