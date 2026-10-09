package mx.edu.analizador;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Lee el contenido completo de un archivo de código C. */
public class LectorC {
    public static String leer(String ruta) throws IOException {
        Path archivo = Path.of(ruta);
        return Files.readString(archivo);
    }
}
