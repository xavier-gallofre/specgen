package com.openapi.generator.database.exporter;

import com.openapi.generator.core.model.OpenApiSpec;
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
}
