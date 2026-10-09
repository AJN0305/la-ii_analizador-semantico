package mx.edu.analizador.lexico;

import java.util.Set;

/**
 * Reconoce las directivas del preprocesador de C. Toda la línea de la directiva es un solo
 * token DIRECTIVA: desde el # hasta el final de la línea, por ejemplo
 * {@code #include <stdio.h>} o {@code #define MAX 10}.
 *
 * Reglas:
 * - El # debe ser el primer carácter no blanco de la línea; un # a mitad de línea no es directiva.
 * - Entre # y el nombre puede haber espacios o tabuladores ({@code # include}).
 * - El nombre debe ser una directiva de C y terminar ahí: #hola, #includes o #define2 no lo son.
 * - Si la línea termina en \ (continuación), la directiva sigue en la línea siguiente.
 * - El salto de línea final no forma parte del lexema.
 *
 * Directivas de varias líneas: el token guarda la línea donde EMPIEZA la directiva,
 * pero {@code fin} es la columna del último carácter en la ÚLTIMA línea. Por ejemplo,
 * {@code #define SUMA(a, b) \} + salto + {@code     ((a) + (b))} da linea = 1, inicio = 1, fin = 15.
 *
 * Pendiente: un comentario al final de la línea ({@code #include <stdio.h> // ...}) por ahora
 * queda dentro del lexema; se separará cuando exista el reconocedor de comentarios.
 *
 * Orden en el Lexer: debe ir ANTES que ReconocedorSimbolo, porque # también es un símbolo.
 */
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

    /** El # es el primer carácter no blanco de su línea (o del archivo). */
    private boolean esInicioDeLinea(Cursor cursor) {
        for (int n = -1; ; n--) {
            char c = cursor.siguiente(n);
            if (c == '\n' || c == '\0') return true;   // '\0' = inicio del archivo
            if (c != ' ' && c != '\t') return false;
        }
    }

    /** Después del # (y de espacios opcionales) viene el nombre de una directiva de C completo. */
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

    /** La línea termina aquí: "\n" o "\r\n" (archivos de Windows). */
    private boolean esFinDeLinea(Cursor cursor) {
        return cursor.actual() == '\n' || (cursor.actual() == '\r' && cursor.siguiente() == '\n');
    }

    /** Hay una \ seguida del salto de línea: "\\\n" o "\\\r\n". */
    private boolean esContinuacionDeLinea(Cursor cursor) {
        if (cursor.actual() != '\\') return false;
        return cursor.siguiente(1) == '\n' || (cursor.siguiente(1) == '\r' && cursor.siguiente(2) == '\n');
    }
}
