package mx.edu.analizador.lexico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ReconocedorCadenaTest {

    private Token leer(String fuente) {
        ReconocedorCadena reconocedor = new ReconocedorCadena();
        Cursor cursor = new Cursor(fuente);
        assertTrue(reconocedor.puedeIniciar(cursor));
        return reconocedor.leer(cursor);
    }

    @Test
    void reconoceCadenaSimple() {
        Token token = leer("\"hola mundo\"");
        assertEquals("\"hola mundo\"", token.lexema());
        assertEquals(TipoToken.CADENA, token.tipo());
        assertEquals(1, token.linea());
        assertEquals(1, token.inicio());
        assertEquals(12, token.fin());
    }

    @Test
    void reconoceCadenaVacia() {
        Token token = leer("\"\"");
        assertEquals("\"\"", token.lexema());
        assertEquals(2, token.fin());
    }

    @Test
    void reconoceEscapesSimples() {
        String fuente = "\"a\\n\\t\\\"b\\\\\"";
        Token token = leer(fuente);
        assertEquals(fuente, token.lexema());
        assertEquals(fuente.length(), token.fin());
    }

    @Test
    void reconoceEscapesNumericos() {
        String fuente = "\"\\x41\\101\\u0041\\U0001F600\"";
        Token token = leer(fuente);
        assertEquals(fuente, token.lexema());
    }

    @Test
    void noIniciaSinComillas() {
        assertFalse(new ReconocedorCadena().puedeIniciar(new Cursor("abc")));
    }

    @Test
    void cadenaSinCerrarLanzaError() {
        assertThrows(IllegalStateException.class, () -> leer("\"abc"));
    }

    @Test
    void saltoDeLineaDentroDeCadenaLanzaError() {
        assertThrows(IllegalStateException.class, () -> leer("\"abc\ndef\""));
    }

    @Test
    void escapeInvalidoLanzaErrorConPosicion() {
        IllegalStateException error =
                assertThrows(IllegalStateException.class, () -> leer("\"a\\qb\""));
        assertTrue(error.getMessage().contains("línea 1, columna 3"));
    }

    @Test
    void hexadecimalSinDigitosLanzaError() {
        assertThrows(IllegalStateException.class, () -> leer("\"\\xZ\""));
    }

    @Test
    void unicodeIncompletoLanzaError() {
        assertThrows(IllegalStateException.class, () -> leer("\"\\u12\""));
        assertThrows(IllegalStateException.class, () -> leer("\"\\U0041\""));
    }

    @Test
    void funcionaDentroDelLexer() {
        Lexer lexer = new Lexer(List.of(new ReconocedorCadena(), new ReconocedorSimbolo()));
        List<Token> tokens = lexer.analizar("(\"hi\")");
        assertEquals(3, tokens.size());
        assertEquals(TipoToken.CADENA, tokens.get(1).tipo());
        assertEquals(2, tokens.get(1).inicio());
        assertEquals(5, tokens.get(1).fin());
    }

    @Test
    void lexerPorDefectoReconoceCadenas() {
        List<Token> tokens = new Lexer().analizar("\"hola\"");
        assertEquals(1, tokens.size());
        assertEquals(TipoToken.CADENA, tokens.get(0).tipo());
    }
}
