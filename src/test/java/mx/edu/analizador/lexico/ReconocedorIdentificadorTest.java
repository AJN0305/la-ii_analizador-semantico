package mx.edu.analizador.lexico;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ReconocedorIdentificadorTest {

    private final ReconocedorIdentificador reconocedor = new ReconocedorIdentificador();

    private Lexer lexer() {
        return new Lexer(List.of(new ReconocedorIdentificador(), new ReconocedorSimbolo()));
    }

    @Test
    void testPalabraReservada() {
        Cursor cursor = new Cursor("int");
        assertTrue(reconocedor.puedeIniciar(cursor));
        assertEquals(new Token("int", TipoToken.PALABRA_RESERVADA, 1, 1, 3), reconocedor.leer(cursor));
        assertTrue(cursor.fin());
    }

    @Test
    void testPalabraReservadaC11() {
        Cursor cursor = new Cursor("_Bool");
        assertEquals(TipoToken.PALABRA_RESERVADA, reconocedor.leer(cursor).tipo());
    }

    @Test
    void testIdentificadoresQueEmpiezanConReservada() {
        List<Token> tokens = lexer().analizar("int2 for_each return_code");

        assertEquals(List.of(
            new Token("int2", TipoToken.IDENTIFICADOR, 1, 1, 4),
            new Token("for_each", TipoToken.IDENTIFICADOR, 1, 6, 13),
            new Token("return_code", TipoToken.IDENTIFICADOR, 1, 15, 25)
        ), tokens);
    }

    @Test
    void testDistingueMayusculas() {
        List<Token> tokens = lexer().analizar("If INT");

        assertEquals(TipoToken.IDENTIFICADOR, tokens.get(0).tipo());
        assertEquals(TipoToken.IDENTIFICADOR, tokens.get(1).tipo());
    }

    @Test
    void testIdentificadoresConDigitosYGuionBajo() {
        List<Token> tokens = lexer().analizar("x1_y2 _");

        assertEquals(List.of(
            new Token("x1_y2", TipoToken.IDENTIFICADOR, 1, 1, 5),
            new Token("_", TipoToken.IDENTIFICADOR, 1, 7, 7)
        ), tokens);
    }

    @Test
    void testNoIniciaConDigito() {
        assertFalse(reconocedor.puedeIniciar(new Cursor("1abc")));
    }

    @Test
    void testDeclaracionConColumnasYLinea() {
        List<Token> tokens = lexer().analizar("\n  int x;");

        assertEquals(List.of(
            new Token("int", TipoToken.PALABRA_RESERVADA, 2, 3, 5),
            new Token("x", TipoToken.IDENTIFICADOR, 2, 7, 7),
            new Token(";", TipoToken.SIMBOLO, 2, 8, 8)
        ), tokens);
    }

    @Test
    void testCaracteresNoValidosEnCLanzanExcepcion() {
        assertThrows(IllegalStateException.class, () -> lexer().analizar("año"));
        assertThrows(IllegalStateException.class, () -> lexer().analizar("a$b"));
        assertThrows(IllegalStateException.class, () -> lexer().analizar("$a"));
    }
}
