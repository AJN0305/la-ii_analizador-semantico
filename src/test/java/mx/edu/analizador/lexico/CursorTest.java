package mx.edu.analizador.lexico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CursorTest {
    @Test
    void recorreElTexto() {
        Cursor c = new Cursor("ab");
        assertEquals('a', c.actual());
        c.avanzar();
        assertEquals('b', c.actual());
        assertFalse(c.fin());
        c.avanzar();
        assertTrue(c.fin());
        assertEquals('\0', c.actual());
    }

    @Test
    void cuentaLineasYColumnas() {
        Cursor c = new Cursor("a\nb");
        assertEquals(1, c.linea());
        assertEquals(1, c.columna());
        c.avanzar();
        assertEquals(2, c.columna());
        c.avanzar();
        assertEquals(2, c.linea());
        assertEquals(1, c.columna());
        assertEquals('b', c.actual());
    }

    @Test
    void siguienteNoFallaAlFinal() {
        Cursor c = new Cursor("ab");
        assertEquals('b', c.siguiente());
        c.avanzar();
        assertEquals('\0', c.siguiente());
        assertEquals('\0', c.siguiente(5));
    }

    @Test
    void avanzarAlFinalNoHaceNada() {
        Cursor c = new Cursor("");
        c.avanzar();
        assertTrue(c.fin());
        assertEquals(1, c.linea());
    }
}
