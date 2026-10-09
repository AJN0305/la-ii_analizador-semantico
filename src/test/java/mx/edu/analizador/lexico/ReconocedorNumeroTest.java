package mx.edu.analizador.lexico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ReconocedorNumeroTest {

    private final ReconocedorNumero reconocedor = new ReconocedorNumero();

    private Token leer(String fuente) {
        Cursor cursor = new Cursor(fuente);
        assertTrue(reconocedor.puedeIniciar(cursor));
        return reconocedor.leer(cursor);
    }

    private void assertNumero(String fuente, TipoToken tipo) {
        Token token = leer(fuente);
        assertEquals(fuente, token.lexema());
        assertEquals(tipo, token.tipo());
        assertEquals(1, token.inicio());
        assertEquals(fuente.length(), token.fin());
    }

    @Test
    void reconoceEnteros() {
        assertNumero("42", TipoToken.CONSTANTE_ENTERA);
        assertNumero("0", TipoToken.CONSTANTE_ENTERA);
        assertNumero("017", TipoToken.CONSTANTE_ENTERA);
        assertNumero("0x1F", TipoToken.CONSTANTE_ENTERA);
        assertNumero("0XabC", TipoToken.CONSTANTE_ENTERA);
    }

    @Test
    void reconoceSufijosDeEnteros() {
        assertNumero("10u", TipoToken.CONSTANTE_ENTERA);
        assertNumero("10L", TipoToken.CONSTANTE_ENTERA);
        assertNumero("10UL", TipoToken.CONSTANTE_ENTERA);
        assertNumero("10llu", TipoToken.CONSTANTE_ENTERA);
        assertNumero("0xFFu", TipoToken.CONSTANTE_ENTERA);
    }

    @Test
    void reconoceReales() {
        assertNumero("3.14", TipoToken.CONSTANTE_REAL);
        assertNumero("7.", TipoToken.CONSTANTE_REAL);
        assertNumero(".5", TipoToken.CONSTANTE_REAL);
        assertNumero("1e10", TipoToken.CONSTANTE_REAL);
        assertNumero("2.5E-3", TipoToken.CONSTANTE_REAL);
        assertNumero("1.e+5", TipoToken.CONSTANTE_REAL);
        assertNumero("3.14f", TipoToken.CONSTANTE_REAL);
        assertNumero("2.0L", TipoToken.CONSTANTE_REAL);
    }

    @Test
    void seDetieneAntesDeLoQueNoEsNumero() {
        Cursor cursor = new Cursor("42;");
        Token token = reconocedor.leer(cursor);
        assertEquals("42", token.lexema());
        assertEquals(';', cursor.actual());
    }

    @Test
    void columnasEnMedioDeLaLinea() {
        Cursor cursor = new Cursor("x = 3.5");
        for (int i = 0; i < 4; i++) cursor.avanzar();
        Token token = reconocedor.leer(cursor);
        assertEquals(new Token("3.5", TipoToken.CONSTANTE_REAL, 1, 5, 7), token);
    }

    @Test
    void noIniciaConPuntoSinDigito() {
        assertFalse(reconocedor.puedeIniciar(new Cursor(".x")));
        assertFalse(reconocedor.puedeIniciar(new Cursor("...")));
        assertFalse(reconocedor.puedeIniciar(new Cursor("abc")));
    }

    @Test
    void numeroPegadoALetrasLanzaError() {
        IllegalStateException error =
                assertThrows(IllegalStateException.class, () -> leer("12abc"));
        assertTrue(error.getMessage().contains("'12abc'"));
        assertTrue(error.getMessage().contains("línea 1, columna 1"));
    }

    @Test
    void numerosMalFormadosLanzanError() {
        assertThrows(IllegalStateException.class, () -> leer("1.5.3"));
        assertThrows(IllegalStateException.class, () -> leer("0x"));
        assertThrows(IllegalStateException.class, () -> leer("08"));
        assertThrows(IllegalStateException.class, () -> leer("10f"));
        assertThrows(IllegalStateException.class, () -> leer("1.5u"));
        assertThrows(IllegalStateException.class, () -> leer("1e"));
    }
}
