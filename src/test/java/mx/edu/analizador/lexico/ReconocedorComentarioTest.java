package mx.edu.analizador.lexico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ReconocedorComentarioTest {

    private final ReconocedorComentario reconocedor =
            new ReconocedorComentario();

    @Test
    void consumeComentarioDeLineaYDejaElSalto() {
        Cursor cursor = new Cursor("// hola mundo\nint x = 1;");

        assertTrue(reconocedor.puedeIniciar(cursor));
        assertNull(reconocedor.leer(cursor));

        // El lexer procesará el salto de línea.
        assertEquals('\n', cursor.actual());
    }

    @Test
    void consumeComentarioDeBloque() {
        Cursor cursor = new Cursor("/* comentario */ int x = 1;");

        assertTrue(reconocedor.puedeIniciar(cursor));
        assertNull(reconocedor.leer(cursor));

        // Queda sobre el espacio después de */
        assertEquals(' ', cursor.actual());
    }

    @Test
    void consumeComentarioDeBloqueMultilinea() {
        Cursor cursor = new Cursor("/* primera linea\nsegunda linea */");

        assertTrue(reconocedor.puedeIniciar(cursor));
        assertNull(reconocedor.leer(cursor));
        assertTrue(cursor.fin());
    }

    @Test
    void consumeComentarioDeBloqueVacio() {
        Cursor cursor = new Cursor("/**/");

        assertNull(reconocedor.leer(cursor));
        assertTrue(cursor.fin());
    }

    @Test
    void consumeComentarioDeBloqueConAsteriscos() {
        Cursor cursor = new Cursor("/* a **/");

        assertNull(reconocedor.leer(cursor));
        assertTrue(cursor.fin());
    }

    @Test
    void comentarioSlashAsteriscoSlashLanzaExcepcion() {
        Cursor cursor = new Cursor("/*/");

        assertThrows(
                IllegalStateException.class,
                () -> reconocedor.leer(cursor)
        );
    }

    @Test
    void comentarioDeBloqueSinCerrarLanzaExcepcion() {
        Cursor cursor = new Cursor("/* sin cerrar");

        assertThrows(
                IllegalStateException.class,
                () -> reconocedor.leer(cursor)
        );
    }

    @Test
    void divisionNoEsComentario() {
        Cursor cursor = new Cursor("/");

        assertFalse(reconocedor.puedeIniciar(cursor));
    }

    @Test
    void divisionAsignacionNoEsComentario() {
        Cursor cursor = new Cursor("/=");

        assertFalse(reconocedor.puedeIniciar(cursor));
    }
}
