package com.specgen.database.exporter;

import com.specgen.core.model.OpenApiSpec;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class SqlInspectorTest {

    @Test
    public void testExportFromSql() {
        String ddl = """
            CREATE TABLE USERS (
                ID NUMBER(10) PRIMARY KEY,
                USERNAME VARCHAR2(50) NOT NULL,
                EMAIL VARCHAR2(100),
                CREATED_AT TIMESTAMP
            );
            
            CREATE INDEX IDX_USERS_USERNAME ON USERS(USERNAME);
        """;

        SqlInspector inspector = new SqlInspector();
        OpenApiSpec spec = inspector.exportFromSql(ddl, List.of("USERS"));

        assertNotNull(spec);
        assertEquals(1, spec.models().size());
        var userModel = spec.models().get(0);
        assertEquals("USERS", userModel.name());
        
        assertTrue(userModel.properties().containsKey("ID"));
        assertTrue(userModel.properties().containsKey("USERNAME"));
        assertTrue(userModel.properties().containsKey("EMAIL"));
        assertTrue(userModel.properties().containsKey("CREATED_AT"));
        
        assertEquals("number", userModel.properties().get("ID").type());
        assertEquals("string", userModel.properties().get("USERNAME").type());
        assertEquals(50, userModel.properties().get("USERNAME").maxLength());
        assertTrue(userModel.properties().get("USERNAME").required());
        assertFalse(userModel.properties().get("EMAIL").required());
    }

    @Test
    public void testExportWithDictionary(@org.junit.jupiter.api.io.TempDir java.nio.file.Path tempDir) throws java.io.IOException {
        String ddl = """
            CREATE TABLE T_USUARIOS (
                USR_ID NUMBER(10) PRIMARY KEY,
                USR_NAME VARCHAR2(50) NOT NULL,
                USR_EMAIL VARCHAR2(100)
            );
        """;

        java.nio.file.Path tableCsv = tempDir.resolve("tables.csv");
        java.nio.file.Files.write(tableCsv, List.of("T_USUARIOS,Usuario"));

        java.nio.file.Path columnCsv = tempDir.resolve("columns.csv");
        java.nio.file.Files.write(columnCsv, List.of(
            "T_USUARIOS.USR_ID,id",
            "USR_NAME,nombre"
        ));

        java.nio.file.Path generalCsv = tempDir.resolve("general.csv");
        java.nio.file.Files.write(generalCsv, List.of("USR_EMAIL,correo"));

        NameDictionary dictionary = new NameDictionary();
        dictionary.loadTableDictionary(tableCsv.toString());
        dictionary.loadColumnDictionary(columnCsv.toString());
        dictionary.loadGeneralDictionary(generalCsv.toString());

        SqlInspector inspector = new SqlInspector();
        inspector.setDictionary(dictionary);
        OpenApiSpec spec = inspector.exportFromSql(ddl, List.of("T_USUARIOS"));

        assertEquals(1, spec.models().size());
        var userModel = spec.models().get(0);
        assertEquals("Usuario", userModel.name());

        assertTrue(userModel.properties().containsKey("id"));
        assertTrue(userModel.properties().containsKey("nombre"));
        assertTrue(userModel.properties().containsKey("correo"));
        assertFalse(userModel.properties().containsKey("USR_ID"));
    }
}
