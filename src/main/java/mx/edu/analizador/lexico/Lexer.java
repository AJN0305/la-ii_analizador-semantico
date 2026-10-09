package mx.edu.analizador.lexico;

import java.util.ArrayList;
import java.util.List;

/**
 * Recorre el código fuente y delega cada token al primer reconocedor que aplique.
 * El ORDEN de la lista importa: por ejemplo, los comentarios deben ir antes que
 * los operadores, y los números antes que los operadores.
 */
public class Lexer {
    private final List<ReconocedorToken> reconocedores;

    /**
     * Constructor por defecto que registra los reconocedores en orden de prioridad.
     * Importante: los comentarios y cadenas deben ir antes que otros tokens,
     * y los números/caracteres antes que operadores.
     */
    public Lexer() {
    this.reconocedores = List.of(
        new ReconocedorComentarioTest(),    // 1. Prioridad máxima: descartar comentarios antes de evaluar '/'
        new ReconocedorDirectiva(),     // 2. Directivas de preprocesador (#include, #define)
        new ReconocedorCadena(),        // 3. Cadenas de texto ("...") y caracteres ('...')
        new ReconocedorCaracter(),      // 3. Cadenas de texto ("...") y caracteres ('...')
        new ReconocedorNumero(),        // 4. Constantes numéricas (enteras y reales)
        new ReconocedorIdentificador(), // 5. Palabras reservadas e identificadores (variables/funciones)
        new ReconocedorOperador(),      // 6. Operadores (+, -, *, ==, etc.)
        new ReconocedorSimbolo()        // 7. Símbolos de puntuación y delimitadores ({}, (), ;, etc.)
    );
}

    public Lexer(List<ReconocedorToken> reconocedores) {
        this.reconocedores = reconocedores;
    }

    public List<Token> analizar(String fuente) {
        Cursor cursor = new Cursor(fuente);
        List<Token> tokens = new ArrayList<>();

        while (!cursor.fin()) {
            if (Character.isWhitespace(cursor.actual())) {
                cursor.avanzar();
                continue;
            }

            ReconocedorToken reconocedor = buscarReconocedor(cursor);
            if (reconocedor == null) {
                throw new IllegalStateException("Carácter inesperado '" + cursor.actual()
                        + "' en línea " + cursor.linea() + ", columna " + cursor.columna());
            }

            Token token = reconocedor.leer(cursor);
            if (token != null) tokens.add(token);
        }
        return tokens;
    }

    private ReconocedorToken buscarReconocedor(Cursor cursor) {
        for (ReconocedorToken r : reconocedores) {
            if (r.puedeIniciar(cursor)) return r;
        }
        return null;
    }
}
