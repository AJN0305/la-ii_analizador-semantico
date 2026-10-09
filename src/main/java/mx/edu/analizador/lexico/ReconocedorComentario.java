package mx.edu.analizador.lexico;

/**
 * Consume comentarios de línea y de bloque sin agregarlos a la lista de tokens.
 */


public class ReconocedorComentario implements ReconocedorToken {

    @Override
    public boolean puedeIniciar(Cursor cursor) {
        return !cursor.fin()
                && cursor.actual() == '/'
                && (cursor.siguiente(1) == '/' || cursor.siguiente(1) == '*');
    }

    @Override
    public Token leer(Cursor cursor) {
        if (!puedeIniciar(cursor)) {
            throw new IllegalArgumentException(
                    "El cursor no está al inicio de un comentario"
            );
        }

        int lineaInicial = cursor.linea();
        int columnaInicial = cursor.columna();

        // Comentario de línea: consume hasta antes del salto de línea
        // o hasta el final del archivo.
        if (cursor.siguiente(1) == '/') {
            cursor.avanzar(); // primer /
            cursor.avanzar(); // segundo /

            while (!cursor.fin() && cursor.actual() != '\n') {
                cursor.avanzar();
            }

            return null;
        }

        // Comentario de bloque: consume primero la apertura /*.
        cursor.avanzar(); // /
        cursor.avanzar(); // *

        // Busca el cierre */ después de consumir la apertura.
        while (!cursor.fin()
                && !(cursor.actual() == '*' && cursor.siguiente(1) == '/')) {
            cursor.avanzar();
        }

        if (cursor.fin()) {
            throw new IllegalStateException(
                    "Comentario sin cerrar iniciado en línea "
                            + lineaInicial
                            + ", columna "
                            + columnaInicial
            );
        }

        cursor.avanzar(); // *
        cursor.avanzar(); // /

        return null;
    }
}