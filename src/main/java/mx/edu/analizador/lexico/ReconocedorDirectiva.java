package mx.edu.analizador.lexico;

import java.util.Set;


public class ReconocedorDirectiva implements ReconocedorToken {

    private static final Set<String> DIRECTIVAS = Set.of(
            "include", "define", "undef",
            "if", "ifdef", "ifndef", "elif", "elifdef", "elifndef", "else", "endif",
            "line", "error", "warning", "pragma", "embed"
    );

    @Override
    public boolean puedeIniciar(Cursor cursor) {
        if (cursor.actual() != '#' || !esInicioDeLinea(cursor)) return false;
        return tieneNombreDeDirectiva(cursor);
    }

    @Override
    public Token leer(Cursor cursor) {
        int linea = cursor.linea();
        int inicio = cursor.columna();
        StringBuilder lexema = new StringBuilder();

        while (!cursor.fin() && !esFinDeLinea(cursor)) {
            if (esContinuacionDeLinea(cursor)) {

                while (cursor.actual() != '\n') {
                    lexema.append(cursor.actual());
                    cursor.avanzar();
                }
            }
            lexema.append(cursor.actual());
            cursor.avanzar();
        }

        int fin = cursor.columna() - 1;
        return new Token(lexema.toString(), TipoToken.DIRECTIVA, linea, inicio, fin);
    }


    private boolean esInicioDeLinea(Cursor cursor) {
        for (int n = -1; ; n--) {
            char c = cursor.siguiente(n);
            if (c == '\n' || c == '\0') return true;
            if (c != ' ' && c != '\t') return false;
        }
    }


    private boolean tieneNombreDeDirectiva(Cursor cursor) {
        int i = 1;
        while (cursor.siguiente(i) == ' ' || cursor.siguiente(i) == '\t') i++;

        StringBuilder nombre = new StringBuilder();
        while (Character.isLetter(cursor.siguiente(i))) {
            nombre.append(cursor.siguiente(i));
            i++;
        }


        char despues = cursor.siguiente(i);
        if (Character.isLetterOrDigit(despues) || despues == '_') return false;

        return DIRECTIVAS.contains(nombre.toString());
    }


    private boolean esFinDeLinea(Cursor cursor) {
        return cursor.actual() == '\n' || (cursor.actual() == '\r' && cursor.siguiente() == '\n');
    }


    private boolean esContinuacionDeLinea(Cursor cursor) {
        if (cursor.actual() != '\\') return false;
        return cursor.siguiente(1) == '\n' || (cursor.siguiente(1) == '\r' && cursor.siguiente(2) == '\n');
    }
}
