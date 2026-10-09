package mx.edu.analizador;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;

public class LectorC {
    public static String Lectura (String ruta) throws IOException {
        Path archivo = Path.of(ruta);
        String contenido = Files.readString(archivo);
        return contenido;

    }
}
