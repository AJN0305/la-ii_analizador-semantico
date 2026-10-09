package mx.edu.analizador.lexico;

import java.util.Set;

/**
 * Reconoce identificadores y palabras reservadas de C11.
 *
 * Lee la palabra completa ([A-Za-z_][A-Za-z0-9_]*) y después consulta la lista:
 * si está, el token es PALABRA_RESERVADA; si no, IDENTIFICADOR. Así "int" es
 * reservada, pero "int2" o "for_each" son identificadores.
 */
public class ReconocedorIdentificador implements ReconocedorToken {

    // palabras reservadas
    private static final Set<String> RESERVADAS = Set.of(
            "auto", "break", "case", "char", "const", "continue",
            "default", "do", "double", "else", "enum", "extern",
            "float", "for", "goto", "if", "inline", "int", "long",
            "register", "restrict", "return", "short", "signed",
            "sizeof", "static", "struct", "switch", "typedef",
            "union", "unsigned", "void", "volatile", "while",
            "_Alignas", "_Alignof", "_Atomic", "_Bool", "_Complex",
            "_Generic", "_Imaginary", "_Noreturn", "_Static_assert",
            "_Thread_local"
    );

    // el identificador empieza con letra ASCII o _
    private static boolean esInicioIdentificador(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    // y sigue con letras, dígitos o _
    private static boolean esParteIdentificador(char c) {
        return esInicioIdentificador(c) || (c >= '0' && c <= '9');
    }

    @Override
    public boolean puedeIniciar(Cursor cursor) {
        return !cursor.fin() && esInicioIdentificador(cursor.actual());
    }

    @Override
    public Token leer(Cursor cursor) {
        // donde comienza la palabra
        int linea = cursor.linea();
        int inicio = cursor.columna();

        // leer el identificador completo
        StringBuilder palabra = new StringBuilder();
        while (!cursor.fin() && esParteIdentificador(cursor.actual())) {
            palabra.append(cursor.actual());
            cursor.avanzar();
        }

        // la columna donde termino
        int fin = cursor.columna() - 1;

        String lexema = palabra.toString();
        TipoToken tipo = RESERVADAS.contains(lexema)
                ? TipoToken.PALABRA_RESERVADA
                : TipoToken.IDENTIFICADOR;

        return new Token(lexema, tipo, linea, inicio, fin);
    }
}
