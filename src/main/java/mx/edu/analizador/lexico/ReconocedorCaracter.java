package mx.edu.analizador.lexico;

public class ReconocedorCaracter implements ReconocedorToken {

    @Override
    public boolean puedeIniciar(Cursor cursor) {
        return cursor.actual() == '\'';
    }

    @Override
    public Token leer(Cursor cursor) {
        int linea = cursor.linea();
        int inicio = cursor.columna();
        StringBuilder lexema = new StringBuilder();

        // Consumir comilla inicial
        lexema.append(cursor.actual());
        cursor.avanzar();

        if (cursor.fin()) {
            throw new IllegalStateException("Carácter no cerrado al final del archivo en línea " + linea + ", columna " + cursor.columna());
        }

        // Salto de línea literal dentro de las comillas no es válido en C
        if (cursor.actual() == '\n' || cursor.actual() == '\r') {
            throw new IllegalStateException("Salto de línea no permitido dentro de constante de carácter en línea " + linea + ", columna " + cursor.columna());
        }

        // Procesar contenido del carácter
        if (cursor.actual() == '\\') {
            // Secuencia de escape
            lexema.append(cursor.actual());
            cursor.avanzar();

            if (cursor.fin()) {
                throw new IllegalStateException("Secuencia de escape incompleta al final del archivo en línea " + linea + ", columna " + cursor.columna());
            }

            char c = cursor.actual();
            if (c == 'n' || c == 't' || c == 'r' || c == '0' || c == '\\' || c == '\'' || c == '"' || c == 'a' || c == 'b' || c == 'f' || c == 'v' || c == '?') {
                lexema.append(c);
                cursor.avanzar();
            } else {
                throw new IllegalStateException("Secuencia de escape no válida '\\" + c + "' en línea " + linea + ", columna " + cursor.columna());
            }
        } else if (cursor.actual() == '\'') {
            throw new IllegalStateException("Constante de carácter vacía '' en línea " + linea + ", columna " + cursor.columna());
        } else {
            // Carácter normal
            lexema.append(cursor.actual());
            cursor.avanzar();
        }

        // Debe cerrar con comilla simple
        if (cursor.fin() || cursor.actual() != '\'') {
            throw new IllegalStateException("Constante de carácter no cerrada con ' en línea " + linea + ", columna " + cursor.columna());
        }

        // Consumir comilla de cierre
        lexema.append(cursor.actual());
        int fin = cursor.columna();
        cursor.avanzar();

        return new Token(lexema.toString(), TipoToken.CONSTANTE_CARACTER, linea, inicio, fin);
    }
}