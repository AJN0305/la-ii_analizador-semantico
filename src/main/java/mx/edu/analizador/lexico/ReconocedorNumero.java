package mx.edu.analizador.lexico;

/**
 * Reconoce las constantes numéricas de C y las clasifica como CONSTANTE_ENTERA o CONSTANTE_REAL.
 *
 * Enteras: decimales (42), octales (017) y hexadecimales (0x1F), con sufijos opcionales
 * u/U y l/L/ll/LL en cualquier orden (10u, 10UL, 10llu).
 * Reales: con punto (3.14, 7., .5), con exponente (1e10, 2.5E-3) o ambos, y sufijo
 * opcional f/F/l/L (3.14f).
 *
 * Si el número va pegado a letras, dígitos, _ o un punto que no le corresponden
 * (12abc, 1.5.3, 0x, 08) lanza un error con la línea y la columna donde empieza.
 *
 * Orden en el Lexer: debe ir ANTES que ReconocedorSimbolo y ReconocedorOperador,
 * porque ".5" empieza con "." (que también es operador).
 */
public class ReconocedorNumero implements ReconocedorToken {

    @Override
    public boolean puedeIniciar(Cursor cursor) {
        if (cursor.fin()) return false;
        char c = cursor.actual();
        return esDigito(c) || (c == '.' && esDigito(cursor.siguiente()));
    }

    @Override
    public Token leer(Cursor cursor) {
        int linea = cursor.linea();
        int inicio = cursor.columna();
        StringBuilder lexema = new StringBuilder();
        boolean esReal = false;

        if (cursor.actual() == '0' && (cursor.siguiente() == 'x' || cursor.siguiente() == 'X')) {
            // Hexadecimal: 0x seguido de al menos un dígito hexadecimal.
            tomar(cursor, lexema);
            tomar(cursor, lexema);
            if (!esHexadecimal(cursor.actual())) {
                throw error(lexema, cursor, linea, inicio);
            }
            while (esHexadecimal(cursor.actual())) tomar(cursor, lexema);
        } else {
            while (esDigito(cursor.actual())) tomar(cursor, lexema);

            // Parte decimal: "7." y ".5" son válidos.
            if (cursor.actual() == '.') {
                esReal = true;
                tomar(cursor, lexema);
                while (esDigito(cursor.actual())) tomar(cursor, lexema);
            }

            // Exponente: e/E, signo opcional y al menos un dígito.
            char e = cursor.actual();
            char despues = cursor.siguiente();
            if ((e == 'e' || e == 'E')
                    && (esDigito(despues)
                    || ((despues == '+' || despues == '-') && esDigito(cursor.siguiente(2))))) {
                esReal = true;
                tomar(cursor, lexema);
                if (!esDigito(cursor.actual())) tomar(cursor, lexema);
                while (esDigito(cursor.actual())) tomar(cursor, lexema);
            }

            // Un octal (empieza con 0) no puede llevar 8 ni 9.
            if (!esReal && lexema.length() > 1 && lexema.charAt(0) == '0'
                    && (lexema.indexOf("8") >= 0 || lexema.indexOf("9") >= 0)) {
                throw error(lexema, cursor, linea, inicio);
            }
        }

        // Sufijos: se leen todas las letras válidas y luego se comprueba la combinación.
        StringBuilder sufijo = new StringBuilder();
        while ("uUlLfF".indexOf(cursor.actual()) >= 0) {
            sufijo.append(cursor.actual());
            tomar(cursor, lexema);
        }
        boolean sufijoValido = esReal
                ? sufijo.toString().matches("[fFlL]?")
                : sufijo.toString().matches("[uU]?(l|L|ll|LL)?|(l|L|ll|LL)[uU]");
        if (!sufijoValido || esParteDeNumero(cursor.actual())) {
            throw error(lexema, cursor, linea, inicio);
        }

        int fin = cursor.columna() - 1;
        TipoToken tipo = esReal ? TipoToken.CONSTANTE_REAL : TipoToken.CONSTANTE_ENTERA;
        return new Token(lexema.toString(), tipo, linea, inicio, fin);
    }

    private void tomar(Cursor cursor, StringBuilder lexema) {
        lexema.append(cursor.actual());
        cursor.avanzar();
    }

    private static boolean esDigito(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean esHexadecimal(char c) {
        return esDigito(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    // Lo que no puede ir pegado al final de un número: letras, dígitos, _ y un punto suelto.
    private static boolean esParteDeNumero(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '.';
    }

    /** Arma el error con todo lo que va pegado al número, para que el mensaje sea claro. */
    private IllegalStateException error(StringBuilder lexema, Cursor cursor, int linea, int columna) {
        StringBuilder malFormado = new StringBuilder(lexema);
        for (int i = 0; esParteDeNumero(cursor.siguiente(i)); i++) {
            malFormado.append(cursor.siguiente(i));
        }
        return new IllegalStateException("Número mal formado '" + malFormado
                + "' en línea " + linea + ", columna " + columna);
    }
}
