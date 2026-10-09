package mx.edu.analizador.lexico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class LexerTest {
    private Lexer lexer() {
        return new Lexer(List.of(new ReconocedorSimbolo()));
    }

    @Test
    void reconoceSimbolosConPosicion() {
        List<Token> tokens = lexer().analizar("(){\n  ;");
        assertEquals(4, tokens.size());
        assertEquals(new Token("(", TipoToken.SIMBOLO, 1, 1, 1), tokens.get(0));
        assertEquals(new Token("{", TipoToken.SIMBOLO, 1, 3, 3), tokens.get(2));
        assertEquals(new Token(";", TipoToken.SIMBOLO, 2, 3, 3), tokens.get(3));
    }

    @Test
    void caracterDesconocidoLanzaExcepcion() {
        assertThrows(IllegalStateException.class, () -> lexer().analizar("(@)"));
    }

    @Test
    void lexerPorDefectoReconoceUnaDeclaracion() {
        assertEquals(List.of(
            new Token("char", TipoToken.PALABRA_RESERVADA, 1, 1, 4),
            new Token("c", TipoToken.IDENTIFICADOR, 1, 6, 6),
            new Token("[", TipoToken.SIMBOLO, 1, 7, 7),
            new Token("]", TipoToken.SIMBOLO, 1, 8, 8),
            new Token(";", TipoToken.SIMBOLO, 1, 9, 9)
        ), new Lexer().analizar("char c[];"));
    }
}
