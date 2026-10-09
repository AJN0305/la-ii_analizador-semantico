package mx.edu.analizador.lexico;


public class ReconocedorSimbolo implements ReconocedorToken {


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


        for (int i = 0; i < lexema.length(); i++) {
            cursor.avanzar();
        }

        int fin = cursor.columna() - 1;
        return new Token(lexema, TipoToken.SIMBOLO, linea, inicio, fin);
    }


    private String buscarSimbolo(Cursor cursor) {
        for (String simbolo : SIMBOLOS) {
            if (coincide(cursor, simbolo)) return simbolo;
        }
        return null;
    }


    private boolean coincide(Cursor cursor, String simbolo) {
        for (int i = 0; i < simbolo.length(); i++) {
            if (cursor.siguiente(i) != simbolo.charAt(i)) return false;
        }
        return true;
    }
}
