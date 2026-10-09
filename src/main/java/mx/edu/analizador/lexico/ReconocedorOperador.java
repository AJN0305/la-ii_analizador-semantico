package mx.edu.analizador.lexico;

/**
 * Reconoce los operadores del lenguaje C: aritméticos (+ - * / %), de incremento (++ --),
 * relacionales (== != < > <= >=), lógicos (&& || !), de bits (& | ^ ~ << >>),
 * de asignación (= += -= *= /= %= &= |= ^= <<= >>=), de acceso a miembros (. ->)
 * y el condicional (? :).
 *
 * Usa la regla de la "pieza más larga": si encuentra "<<=", lo toma completo
 * en lugar de quedarse solo con "<" o con "<<".
 *
 * Orden en el Lexer: los reconocedores de comentarios y de números deben ir ANTES que este,
 * porque "//" y "/*" empiezan con "/", y un número como ".5" empieza con ".".
 */
public class ReconocedorOperador implements ReconocedorToken {

    // Ordenados de MAYOR a MENOR longitud: así siempre se prueba primero el más largo.
    private static final String[] OPERADORES = {
            // 3 caracteres
            "<<=", ">>=",
            // 2 caracteres
            "++", "--", "==", "!=", "<=", ">=", "&&", "||", "<<", ">>",
            "+=", "-=", "*=", "/=", "%=", "&=", "|=", "^=", "->",
            // 1 carácter
            "+", "-", "*", "/", "%", "=", "<", ">", "!", "&", "|", "^", "~", "?", ":", "."
    };

    @Override
    public boolean puedeIniciar(Cursor cursor) {
        return buscarOperador(cursor) != null;
    }

    @Override
    public Token leer(Cursor cursor) {
        int linea = cursor.linea();
        int inicio = cursor.columna();
        String lexema = buscarOperador(cursor);

        // Avanzamos tantos caracteres como mida el operador encontrado.
        for (int i = 0; i < lexema.length(); i++) {
            cursor.avanzar();
        }

        int fin = cursor.columna() - 1;
        return new Token(lexema, TipoToken.OPERADOR, linea, inicio, fin);
    }

    /** Devuelve el operador más largo que empieza en la posición actual, o null si no hay ninguno. */
    private String buscarOperador(Cursor cursor) {
        for (String operador : OPERADORES) {
            if (coincide(cursor, operador)) return operador;
        }
        return null;
    }

    /** Compara, sin avanzar el cursor, si el texto que sigue es igual al operador. */
    private boolean coincide(Cursor cursor, String operador) {
        for (int i = 0; i < operador.length(); i++) {
            if (cursor.siguiente(i) != operador.charAt(i)) return false;
        }
        return true;
    }
}
