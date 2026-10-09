package mx.edu.analizador.lexico;

import java.util.ArrayList;
import java.util.List;

/**
 * Recorre el código fuente y delega cada token al primer reconocedor que aplique.
 * El orden de la lista importa.
 */
public class Lexer {
    private final List<ReconocedorToken> reconocedores;

    /**
     * Registra los reconocedores en orden de prioridad.
     */
    public Lexer() {
        this(List.of(
                new ReconocedorComentario(),
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
                throw new IllegalStateException(
                        "Carácter inesperado '" + cursor.actual()
                                + "' en línea " + cursor.linea()
                                + ", columna " + cursor.columna()
                );
            }

            Token token = reconocedor.leer(cursor);

            // Los comentarios devuelven null y no se agregan a la tabla.
            if (token != null) {
                tokens.add(token);
            }
        }

        return tokens;
    }

    private ReconocedorToken buscarReconocedor(Cursor cursor) {
        for (ReconocedorToken reconocedor : reconocedores) {
            if (reconocedor.puedeIniciar(cursor)) {
                return reconocedor;
            }
        }

        return null;
    }
}