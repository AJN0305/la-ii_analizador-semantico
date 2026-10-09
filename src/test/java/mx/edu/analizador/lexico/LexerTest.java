package mx.edu.analizador.lexico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class LexerTest {

    private Lexer lexer() {
        return new Lexer(List.of(
                new ReconocedorComentario(),
                new ReconocedorCaracter(),
                new ReconocedorIdentificador(),
                new ReconocedorSimbolo()
        ));
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
        assertThrows(
                IllegalStateException.class,
                () -> lexer().analizar("(@)")
        );
    }

    @Test
    void conservaLineaYColumnaDespuesDeComentarioDeLinea() {
        List<Token> tokens = lexer().analizar("x; // fin\ny;");

        Token y = tokens.stream()
                .filter(token -> token.lexema().equals("y"))
                .findFirst()
                .orElseThrow();

        assertEquals(2, y.linea());
        assertEquals(1, y.inicio());
    }

    @Test
    void conservaLineaYColumnaDespuesDeComentarioDeBloqueMultilinea() {
        List<Token> tokens = lexer().analizar("/* a\n b */ x");

        Token x = tokens.stream()
                .filter(token -> token.lexema().equals("x"))
                .findFirst()
                .orElseThrow();

        assertEquals(2, x.linea());
        assertEquals(7, x.inicio());
    }
}