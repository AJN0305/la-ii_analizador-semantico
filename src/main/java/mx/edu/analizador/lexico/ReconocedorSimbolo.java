package mx.edu.analizador.lexico;

/**
 * Reconoce los símbolos de puntuación del lenguaje C que no son operadores:
 * de agrupación ( ) [ ] { }, separadores ; y , , los puntos suspensivos ...
 * de las funciones con parámetros variables (por ejemplo printf), y # y ##,
 * que se usan dentro de las macros.
 *
 * Usa la regla de la "pieza más larga": "..." se toma completo y "##" no se parte en dos "#".
 *
 * Orden en el Lexer: debe ir ANTES que ReconocedorOperador, para que "..." no se lea
 * como tres operadores "."; y DESPUÉS del reconocedor de directivas, para que "#include"
 * se reconozca como directiva y no como el símbolo "#".
 */
public class ReconocedorSimbolo implements ReconocedorToken {

    // Ordenados de MAYOR a MENOR longitud: así siempre se prueba primero el más largo.
    private static final String[] SIMBOLOS = {
            // 3 caracteres
            "...",
            // 2 caracteres
            "##",
            // 1 carácter
            "(", ")", "{", "}", "[", "]", ";", ",", "#"
    };

    @Override
    public boolean puedeIniciar(Cursor cursor) {
        return buscarSimbolo(cursor) != null;
    }

    @Override
    public Token leer(Cursor cursor) {
        int linea = cursor.linea();
        int inicio = cursor.columna();
        String lexema = buscarSimbolo(cursor);

        // Avanzamos tantos caracteres como mida el símbolo encontrado.
        for (int i = 0; i < lexema.length(); i++) {
            cursor.avanzar();
        }

        int fin = cursor.columna() - 1;
        return new Token(lexema, TipoToken.SIMBOLO, linea, inicio, fin);
    }

    /** Devuelve el símbolo más largo que empieza en la posición actual, o null si no hay ninguno. */
    private String buscarSimbolo(Cursor cursor) {
        for (String simbolo : SIMBOLOS) {
            if (coincide(cursor, simbolo)) return simbolo;
        }
        return null;
    }

    /** Compara, sin avanzar el cursor, si el texto que sigue es igual al símbolo. */
    private boolean coincide(Cursor cursor, String simbolo) {
        for (int i = 0; i < simbolo.length(); i++) {
            if (cursor.siguiente(i) != simbolo.charAt(i)) return false;
        }
        return true;
    }
}
