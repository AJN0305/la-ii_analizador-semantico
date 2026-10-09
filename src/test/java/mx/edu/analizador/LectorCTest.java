package mx.edu.analizador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LectorCTest {

    @TempDir
    Path carpeta;

    @Test
    void leeElContenidoCompleto() throws IOException {
        Path archivo = carpeta.resolve("ejemplo.c");
        String codigo = "#include <stdio.h>\n\nint main(void) {\n    return 0;\n}\n";
        Files.writeString(archivo, codigo);

        assertEquals(codigo, LectorC.leer(archivo.toString()));
    }

    @Test
    void conservaAcentosEnUtf8() throws IOException {
        Path archivo = carpeta.resolve("acentos.c");
        Files.writeString(archivo, "// función con ñ\n");

        assertEquals("// función con ñ\n", LectorC.leer(archivo.toString()));
    }

    @Test
    void archivoInexistenteLanzaExcepcion() {
        Path archivo = carpeta.resolve("no-existe.c");

        assertThrows(IOException.class, () -> LectorC.leer(archivo.toString()));
    }
}
