package mx.edu.analizador.lexico;

/** Reconoce si el caracter actual es un número entero o un número decimal */
public class ReconocedorNumero implements ReconocedorToken {
    @Override
    public boolean puedeIniciar(Cursor cursor) {
        return !cursor.fin() && (Character.isDigit(cursor.actual()));
    }

    @Override
    public Token leer(Cursor cursor) {
        int linea = cursor.linea();
        int inicio = cursor.columna();
        StringBuilder lexemaSB = new StringBuilder();
        boolean esDecimal = false;

        while (!cursor.fin()) {
            char c = cursor.actual();
            if (Character.isDigit(c)) {
                lexemaSB.append(c);
                cursor.avanzar();
            } else if (c == '.' && !esDecimal && Character.isDigit(cursor.siguiente())) {
                esDecimal = true;
                lexemaSB.append(c);
                cursor.avanzar();
            } else {
                break;
            }
        }

        int fin = cursor.columna() - 1;
        String lexema = lexemaSB.toString();

        if (esDecimal) {
            return new Token(lexema, TipoToken.CONSTANTE_REAL, linea, inicio, fin);
        } else {
            return new Token(lexema, TipoToken.CONSTANTE_ENTERA, linea, inicio, fin);

        }
    }

}
