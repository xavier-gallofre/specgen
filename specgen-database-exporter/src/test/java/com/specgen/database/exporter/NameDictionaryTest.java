package com.specgen.database.exporter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NameDictionaryTest {

    @TempDir
    Path tempDir;

    @Test
    void testResolveTableName() throws IOException {
        Path tableCsv = tempDir.resolve("tables.csv");
        Files.write(tableCsv, List.of("USERS,Usuario", "ORDERS,Pedido"));

        Path generalCsv = tempDir.resolve("general.csv");
        Files.write(generalCsv, List.of("USERS,Gente", "STATUS,Estado"));

        NameDictionary dictionary = new NameDictionary();
        dictionary.loadTableDictionary(tableCsv.toString());
        dictionary.loadGeneralDictionary(generalCsv.toString());

        // Prioridad Tabla > General
        assertEquals("Usuario", dictionary.resolveTableName("USERS"));
        // Solo en general
        assertEquals("Estado", dictionary.resolveTableName("STATUS"));
        // Ninguno
        assertEquals("UNKNOWN", dictionary.resolveTableName("UNKNOWN"));
    }

    @Test
    void testResolveColumnName() throws IOException {
        Path columnCsv = tempDir.resolve("columns.csv");
        Files.write(columnCsv, List.of(
            "USERS.NAME,NombreUsuario", 
            "NAME,NombreGenerico",
            "ID,Identificador"
        ));

        Path generalCsv = tempDir.resolve("general.csv");
        Files.write(generalCsv, List.of("ID,G-ID", "DESC,Descripcion"));

        NameDictionary dictionary = new NameDictionary();
        dictionary.loadColumnDictionary(columnCsv.toString());
        dictionary.loadGeneralDictionary(generalCsv.toString());

        // Prioridad: Columna específica > Columna general > General
        
        // Específica vs General Columna
        assertEquals("NombreUsuario", dictionary.resolveColumnName("USERS", "NAME"));
        
        // Solo Columna general vs General
        assertEquals("Identificador", dictionary.resolveColumnName("ORDERS", "ID"));
        
        // Solo General
        assertEquals("Descripcion", dictionary.resolveColumnName("PRODUCTS", "DESC"));
        
        // Ninguno
        assertEquals("PRICE", dictionary.resolveColumnName("PRODUCTS", "PRICE"));
    }
}
