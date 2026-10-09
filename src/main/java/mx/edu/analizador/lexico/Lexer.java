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
        this(List.of(
            new ReconocedorCaracter(),
            new ReconocedorIdentificador(),
            new ReconocedorSimbolo()
        ));
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
