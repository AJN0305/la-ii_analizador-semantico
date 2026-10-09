package mx.edu.analizador.lexico;


public class ReconocedorOperador implements ReconocedorToken {


    private static final String[] OPERADORES = {

            "<<=", ">>=",
            "++", "--", "==", "!=", "<=", ">=", "&&", "||", "<<", ">>",
            "+=", "-=", "*=", "/=", "%=", "&=", "|=", "^=", "->",
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


        for (int i = 0; i < lexema.length(); i++) {
            cursor.avanzar();
        }

        int fin = cursor.columna() - 1;
        return new Token(lexema, TipoToken.OPERADOR, linea, inicio, fin);
    }


    private String buscarOperador(Cursor cursor) {
        for (String operador : OPERADORES) {
            if (coincide(cursor, operador)) return operador;
        }
        return null;
    }


    private boolean coincide(Cursor cursor, String operador) {
        for (int i = 0; i < operador.length(); i++) {
            if (cursor.siguiente(i) != operador.charAt(i)) return false;
        }
        return true;
    }
}
