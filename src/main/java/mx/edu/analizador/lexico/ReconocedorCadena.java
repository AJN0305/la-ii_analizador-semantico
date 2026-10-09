package mx.edu.analizador.lexico;

/**
 * Reconoce las cadenas de C entre comillas dobles, como "hola", y las marca como CADENA.
 * El lexema incluye las comillas.
 *
 * Acepta las secuencias de escape de C: simples (\n \t \r \b \f \v \a \\ \" \' \?),
 * octales de 1 a 3 dígitos (\101), hexadecimales (\x41) y universales (\u0041, \U0001F600).
 *
 * Lanza un error con línea y columna si la cadena no se cierra antes del salto de línea
 * o del fin del archivo, o si una secuencia de escape no es válida.
 *
 * No soporta los prefijos L, u, U ni u8: L"hola" se lee como el identificador L y la cadena.
 *
 * Orden en el Lexer: debe ir ANTES que ReconocedorCaracter, para que la ' dentro de
 * una cadena no se lea como inicio de un carácter.
 */
public class ReconocedorCadena implements ReconocedorToken {
    private static final char COMILLA = '"';
    private static final char ESCAPE = '\\';
    private static final String ESCAPES_SIMPLES = "ntrbfva\\\"'?";

    @Override
    public boolean puedeIniciar(Cursor cursor) {
        return !cursor.fin() && cursor.actual() == COMILLA;
    }

    @Override
    public Token leer(Cursor cursor) {
        int linea = cursor.linea();
        int inicio = cursor.columna();
        StringBuilder lexema = new StringBuilder();

        tomar(cursor, lexema);

        while (true) {
            if (cursor.fin() || cursor.actual() == '\n' || cursor.actual() == '\r') {
                throw error("Cadena sin cerrar", linea, inicio);
            }

            char c = cursor.actual();
            if (c == COMILLA) {
                tomar(cursor, lexema);
                break;
            }
            if (c == ESCAPE) {
                leerEscape(cursor, lexema);
            } else {
                tomar(cursor, lexema);
            }
        }

        int fin = cursor.columna() - 1;
        return new Token(lexema.toString(), TipoToken.CADENA, linea, inicio, fin);
    }

    private void leerEscape(Cursor cursor, StringBuilder lexema) {
        int linea = cursor.linea();
        int columna = cursor.columna();

        tomar(cursor, lexema);

        if (cursor.fin() || cursor.actual() == '\n' || cursor.actual() == '\r') {
            return;
        }

        char c = cursor.actual();
        if (ESCAPES_SIMPLES.indexOf(c) >= 0) {
            tomar(cursor, lexema);
        } else if (esOctal(c)) {
            int digitos = 0;
            while (digitos < 3 && esOctal(cursor.actual())) {
                tomar(cursor, lexema);
                digitos++;
            }
        } else if (c == 'x') {
            tomar(cursor, lexema);
            if (!esHexadecimal(cursor.actual())) {
                throw error("Escape \\x sin dígitos hexadecimales", linea, columna);
            }
            while (esHexadecimal(cursor.actual())) {
                tomar(cursor, lexema);
            }
        } else if (c == 'u' || c == 'U') {
            int digitos = c == 'u' ? 4 : 8;
            tomar(cursor, lexema);
            for (int i = 0; i < digitos; i++) {
                if (!esHexadecimal(cursor.actual())) {
                    throw error("Escape \\" + c + " incompleto, se necesitan " + digitos
                            + " dígitos hexadecimales", linea, columna);
                }
                tomar(cursor, lexema);
            }
        } else {
            throw error("Secuencia de escape inválida '\\" + c + "'", linea, columna);
        }
    }

    private void tomar(Cursor cursor, StringBuilder lexema) {
        lexema.append(cursor.actual());
        cursor.avanzar();
    }

    private boolean esOctal(char c) {
        return c >= '0' && c <= '7';
    }

    private boolean esHexadecimal(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private IllegalStateException error(String mensaje, int linea, int columna) {
        return new IllegalStateException(mensaje + " en línea " + linea + ", columna " + columna);
    }
}
