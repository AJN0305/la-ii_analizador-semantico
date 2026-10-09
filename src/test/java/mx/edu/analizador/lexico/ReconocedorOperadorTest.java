package mx.edu.analizador.lexico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ReconocedorOperadorTest {
    private Lexer lexer() {
        return new Lexer(List.of(new ReconocedorSimbolo(), new ReconocedorOperador()));
    }

    // ---------- Caso normal ----------

    @Test
    void reconoceOperadoresDeUnCaracter() {
        List<Token> tokens = lexer().analizar("+ - * / %");
        assertEquals(5, tokens.size());
        assertEquals(new Token("+", TipoToken.OPERADOR, 1, 1, 1), tokens.get(0));
        assertEquals(new Token("%", TipoToken.OPERADOR, 1, 9, 9), tokens.get(4));
    }

    @Test
    void convivenConLosSimbolosYGuardanLaLinea() {
        List<Token> tokens = lexer().analizar("(!=)\n&&;");
        assertEquals(5, tokens.size());
        assertEquals(new Token("(", TipoToken.SIMBOLO, 1, 1, 1), tokens.get(0));
        assertEquals(new Token("!=", TipoToken.OPERADOR, 1, 2, 3), tokens.get(1));
        assertEquals(new Token(")", TipoToken.SIMBOLO, 1, 4, 4), tokens.get(2));
        assertEquals(new Token("&&", TipoToken.OPERADOR, 2, 1, 2), tokens.get(3));
        assertEquals(new Token(";", TipoToken.SIMBOLO, 2, 3, 3), tokens.get(4));
    }

    // ---------- Casos borde: la pieza más larga ----------

    @Test
    void tomaSiempreElOperadorMasLargo() {
        List<Token> tokens = lexer().analizar("<<= << <= <");
        assertEquals(4, tokens.size());
        assertEquals(new Token("<<=", TipoToken.OPERADOR, 1, 1, 3), tokens.get(0));
        assertEquals(new Token("<<", TipoToken.OPERADOR, 1, 5, 6), tokens.get(1));
        assertEquals(new Token("<=", TipoToken.OPERADOR, 1, 8, 9), tokens.get(2));
        assertEquals(new Token("<", TipoToken.OPERADOR, 1, 11, 11), tokens.get(3));
    }

    @Test
    void distingueLasVariantesDelMenos() {
        List<Token> tokens = lexer().analizar("- -- -= ->");
        assertEquals(4, tokens.size());
        assertEquals(new Token("-", TipoToken.OPERADOR, 1, 1, 1), tokens.get(0));
        assertEquals(new Token("--", TipoToken.OPERADOR, 1, 3, 4), tokens.get(1));
        assertEquals(new Token("-=", TipoToken.OPERADOR, 1, 6, 7), tokens.get(2));
        assertEquals(new Token("->", TipoToken.OPERADOR, 1, 9, 10), tokens.get(3));
    }

    @Test
    void operadoresPegadosSeSeparanBien() {
        // "++==" debe salir como "++" y "=="
        List<Token> tokens = lexer().analizar("++==");
        assertEquals(2, tokens.size());
        assertEquals(new Token("++", TipoToken.OPERADOR, 1, 1, 2), tokens.get(0));
        assertEquals(new Token("==", TipoToken.OPERADOR, 1, 3, 4), tokens.get(1));
    }

    // ---------- Casos borde: final del texto ----------

    @Test
    void operadorAlFinalDelTexto() {
        // Al final no hay más caracteres: ">" no debe confundirse con ">=" ni ">>"
        List<Token> tokens = lexer().analizar(">");
        assertEquals(1, tokens.size());
        assertEquals(new Token(">", TipoToken.OPERADOR, 1, 1, 1), tokens.get(0));
    }

    // ---------- Casos de la revisión, con identificadores ----------

    private Lexer lexerConPalabras() {
        return new Lexer(List.of(new ReconocedorIdentificador(), new ReconocedorSimbolo(), new ReconocedorOperador()));
    }

    @Test
    void flechaYCorrimientoConAsignacion() {
        assertEquals(List.of(
                new Token("p", TipoToken.IDENTIFICADOR, 1, 1, 1),
                new Token("->", TipoToken.OPERADOR, 1, 2, 3),
                new Token("x", TipoToken.IDENTIFICADOR, 1, 4, 4),
                new Token("<<=", TipoToken.OPERADOR, 1, 6, 8),
                new Token("n", TipoToken.IDENTIFICADOR, 1, 10, 10)
        ), lexerConPalabras().analizar("p->x <<= n"));
    }

    @Test
    void operadorPegadoAIdentificadores() {
        assertEquals(List.of(
                new Token("a", TipoToken.IDENTIFICADOR, 1, 1, 1),
                new Token(">>=", TipoToken.OPERADOR, 1, 2, 4),
                new Token("b", TipoToken.IDENTIFICADOR, 1, 5, 5)
        ), lexerConPalabras().analizar("a>>=b"));
    }

    @Test
    void yLogicoSeguidoDeNegacion() {
        assertEquals(List.of(
                new Token("x", TipoToken.IDENTIFICADOR, 1, 1, 1),
                new Token("&&", TipoToken.OPERADOR, 1, 2, 3),
                new Token("!", TipoToken.OPERADOR, 1, 4, 4),
                new Token("y", TipoToken.IDENTIFICADOR, 1, 5, 5)
        ), lexerConPalabras().analizar("x&&!y"));
    }

    // ---------- Reglas de la interfaz ----------

    @Test
    void puedeIniciarNoAvanzaElCursor() {
        Cursor cursor = new Cursor(">>=");
        assertTrue(new ReconocedorOperador().puedeIniciar(cursor));
        assertEquals(1, cursor.columna());
    }

    @Test
    void puedeIniciarRechazaLoQueNoEsOperador() {
        ReconocedorOperador reconocedor = new ReconocedorOperador();
        assertFalse(reconocedor.puedeIniciar(new Cursor("a")));
        assertFalse(reconocedor.puedeIniciar(new Cursor("5")));
        assertFalse(reconocedor.puedeIniciar(new Cursor("(")));
        assertFalse(reconocedor.puedeIniciar(new Cursor("")));
    }

    @Test
    void leerConsumeSoloElOperador() {
        Cursor cursor = new Cursor("+=5");
        Token token = new ReconocedorOperador().leer(cursor);
        assertEquals(new Token("+=", TipoToken.OPERADOR, 1, 1, 2), token);
        assertEquals('5', cursor.actual());
        assertEquals(3, cursor.columna());
    }
}
