package mx.edu.analizador.lexico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ReconocedorSimboloTest {

    private Lexer lexer() {
        return new Lexer(List.of(new ReconocedorSimbolo(), new ReconocedorOperador()));
    }



    @Test
    void reconoceSimbolosDeAgrupacionYSeparadores() {
        List<Token> tokens = lexer().analizar("( ) { } [ ] ; ,");
        assertEquals(8, tokens.size());
        assertEquals(new Token("(", TipoToken.SIMBOLO, 1, 1, 1), tokens.get(0));
        assertEquals(new Token("{", TipoToken.SIMBOLO, 1, 5, 5), tokens.get(2));
        assertEquals(new Token("]", TipoToken.SIMBOLO, 1, 11, 11), tokens.get(5));
        assertEquals(new Token(",", TipoToken.SIMBOLO, 1, 15, 15), tokens.get(7));
    }

    @Test
    void guardaLaLineaCorrecta() {
        List<Token> tokens = lexer().analizar("{\n  }");
        assertEquals(2, tokens.size());
        assertEquals(new Token("{", TipoToken.SIMBOLO, 1, 1, 1), tokens.get(0));
        assertEquals(new Token("}", TipoToken.SIMBOLO, 2, 3, 3), tokens.get(1));
    }


    @Test
    void puntosSuspensivosSonUnSoloSimbolo() {

        List<Token> tokens = lexer().analizar("(, ...)");
        assertEquals(4, tokens.size());
        assertEquals(new Token("(", TipoToken.SIMBOLO, 1, 1, 1), tokens.get(0));
        assertEquals(new Token(",", TipoToken.SIMBOLO, 1, 2, 2), tokens.get(1));
        assertEquals(new Token("...", TipoToken.SIMBOLO, 1, 4, 6), tokens.get(2));
        assertEquals(new Token(")", TipoToken.SIMBOLO, 1, 7, 7), tokens.get(3));
    }

    @Test
    void unPuntoSueltoSigueSiendoOperador() {

        List<Token> tokens = lexer().analizar("... .");
        assertEquals(2, tokens.size());
        assertEquals(new Token("...", TipoToken.SIMBOLO, 1, 1, 3), tokens.get(0));
        assertEquals(new Token(".", TipoToken.OPERADOR, 1, 5, 5), tokens.get(1));
    }

    @Test
    void dosPuntosNoFormanPuntosSuspensivos() {

        List<Token> tokens = lexer().analizar("..");
        assertEquals(2, tokens.size());
        assertEquals(new Token(".", TipoToken.OPERADOR, 1, 1, 1), tokens.get(0));
        assertEquals(new Token(".", TipoToken.OPERADOR, 1, 2, 2), tokens.get(1));
    }

    @Test
    void dobleNumeralEsUnSoloSimbolo() {
        List<Token> tokens = lexer().analizar("## #");
        assertEquals(2, tokens.size());
        assertEquals(new Token("##", TipoToken.SIMBOLO, 1, 1, 2), tokens.get(0));
        assertEquals(new Token("#", TipoToken.SIMBOLO, 1, 4, 4), tokens.get(1));
    }



    @Test
    void simboloAlFinalDelTexto() {
        List<Token> tokens = lexer().analizar("]");
        assertEquals(1, tokens.size());
        assertEquals(new Token("]", TipoToken.SIMBOLO, 1, 1, 1), tokens.get(0));
    }



    private Lexer lexerConPalabras() {
        return new Lexer(List.of(new ReconocedorIdentificador(), new ReconocedorSimbolo(), new ReconocedorOperador()));
    }

    @Test
    void puntosSuspensivosEnUnaFuncion() {
        assertEquals(List.of(
                new Token("f", TipoToken.IDENTIFICADOR, 1, 1, 1),
                new Token("(", TipoToken.SIMBOLO, 1, 2, 2),
                new Token("int", TipoToken.PALABRA_RESERVADA, 1, 3, 5),
                new Token(",", TipoToken.SIMBOLO, 1, 6, 6),
                new Token("...", TipoToken.SIMBOLO, 1, 8, 10),
                new Token(")", TipoToken.SIMBOLO, 1, 11, 11)
        ), lexerConPalabras().analizar("f(int, ...)"));
    }

    @Test
    void dobleNumeralEntreIdentificadores() {
        assertEquals(List.of(
                new Token("a", TipoToken.IDENTIFICADOR, 1, 1, 1),
                new Token("##", TipoToken.SIMBOLO, 1, 3, 4),
                new Token("b", TipoToken.IDENTIFICADOR, 1, 6, 6)
        ), lexerConPalabras().analizar("a ## b"));
    }



    @Test
    void puedeIniciarNoAvanzaElCursor() {
        Cursor cursor = new Cursor("...");
        assertTrue(new ReconocedorSimbolo().puedeIniciar(cursor));
        assertEquals(1, cursor.columna());
    }

    @Test
    void puedeIniciarRechazaLoQueNoEsSimbolo() {
        ReconocedorSimbolo reconocedor = new ReconocedorSimbolo();
        assertFalse(reconocedor.puedeIniciar(new Cursor("a")));
        assertFalse(reconocedor.puedeIniciar(new Cursor("+")));
        assertFalse(reconocedor.puedeIniciar(new Cursor(".")));
        assertFalse(reconocedor.puedeIniciar(new Cursor("")));
    }

    @Test
    void leerConsumeSoloElSimbolo() {
        Cursor cursor = new Cursor("...)");
        Token token = new ReconocedorSimbolo().leer(cursor);
        assertEquals(new Token("...", TipoToken.SIMBOLO, 1, 1, 3), token);
        assertEquals(')', cursor.actual());
        assertEquals(4, cursor.columna());
    }
}
