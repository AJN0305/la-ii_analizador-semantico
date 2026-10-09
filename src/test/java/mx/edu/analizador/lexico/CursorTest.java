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

    private final ReconocedorNumero reconocedorNumero = new ReconocedorNumero();

    @Test
    void testEnteroSimple() {
        Cursor c = new Cursor("42");
        assertTrue(reconocedorNumero.puedeIniciar(c));
        Token token = reconocedorNumero.leer(c);

        assertEquals("42", token.lexema());
        assertEquals(TipoToken.CONSTANTE_ENTERA, token.tipo());
        assertEquals(1, token.inicio());
        assertEquals(2, token.fin());
    }

    @Test
    void testRealSimple() {
        Cursor c = new Cursor("3.14");
        Token token = reconocedorNumero.leer(c);

        assertEquals("3.14", token.lexema());
        assertEquals(TipoToken.CONSTANTE_REAL, token.tipo());
    }

    @Test
    void testRealDobleDecimal() {
        Cursor c = new Cursor("1.5.3");
        Token token = reconocedorNumero.leer(c);

        assertEquals("1.5", token.lexema());
        assertEquals(TipoToken.CONSTANTE_REAL, token.tipo());
        assertEquals('.', c.actual());
    }

    @Test
    void testPuntoAlFinal() {
        Cursor c = new Cursor("7.");
        Token token = reconocedorNumero.leer(c);

        assertEquals("7", token.lexema());
        assertEquals(TipoToken.CONSTANTE_ENTERA, token.tipo());
        assertEquals('.', c.actual());
    }
 }
