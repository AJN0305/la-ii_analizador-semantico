package mx.edu.analizador.lexico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ReconocedorDirectivaTest {
    // Mismo orden que en Lexer(): identificadores, directivas, símbolos, operadores.
    private Lexer lexer() {
        return new Lexer(List.of(
                new ReconocedorIdentificador(),
                new ReconocedorDirectiva(),
                new ReconocedorSimbolo(),
                new ReconocedorOperador()));
    }

    private Token directiva(String lexema, int linea, int inicio, int fin) {
        return new Token(lexema, TipoToken.DIRECTIVA, linea, inicio, fin);
    }


    @Test
    void includeCompletoEsUnSoloToken() {
        assertEquals(List.of(directiva("#include <stdio.h>", 1, 1, 18)),
                lexer().analizar("#include <stdio.h>"));
    }

    @Test
    void defineConValorEsUnSoloToken() {
        assertEquals(List.of(directiva("#define MAX 10", 1, 1, 14)),
                lexer().analizar("#define MAX 10"));
    }

    @Test
    void defineConNumeralDentroEsUnSoloToken() {
        assertEquals(List.of(directiva("#define F(a) #a", 1, 1, 15)),
                lexer().analizar("#define F(a) #a"));
    }

    @Test
    void continuacionDeLineaAbarcaAmbasLineas() {
        String fuente = "#define SUMA(a, b) \\\n    ((a) + (b))\n;";
        assertEquals(List.of(
                directiva("#define SUMA(a, b) \\\n    ((a) + (b))", 1, 1, 15),
                new Token(";", TipoToken.SIMBOLO, 3, 1, 1)
        ), lexer().analizar(fuente));
    }

    // ---------- El salto de línea final no es parte del lexema ----------

    @Test
    void saltoDeLineaFinalNoEsParteDelLexema() {
        assertEquals(List.of(
                directiva("#endif", 1, 1, 6),
                new Token(";", TipoToken.SIMBOLO, 2, 1, 1)
        ), lexer().analizar("#endif\n;"));
    }

    @Test
    void saltoDeLineaDeWindowsNoEsParteDelLexema() {
        assertEquals(List.of(
                directiva("#endif", 1, 1, 6),
                new Token(";", TipoToken.SIMBOLO, 2, 1, 1)
        ), lexer().analizar("#endif\r\n;"));
    }

    @Test
    void continuacionDeLineaConSaltoDeWindows() {
        assertEquals(List.of(
                directiva("#define A \\\r\n 1", 1, 1, 2),
                new Token(";", TipoToken.SIMBOLO, 3, 1, 1)
        ), lexer().analizar("#define A \\\r\n 1\r\n;"));
    }

    @Test
    void variasDirectivasEnLineasDistintas() {
        assertEquals(List.of(
                directiva("#ifdef DEBUG", 2, 1, 12),
                directiva("#endif", 3, 1, 6)
        ), lexer().analizar("\n#ifdef DEBUG\n#endif"));
    }



    @Test
    void numeralAMitadDeLineaNoEsDirectiva() {
        // a #if b  ->  a, #, if, b
        assertEquals(List.of(
                new Token("a", TipoToken.IDENTIFICADOR, 1, 1, 1),
                new Token("#", TipoToken.SIMBOLO, 1, 3, 3),
                new Token("if", TipoToken.PALABRA_RESERVADA, 1, 4, 5),
                new Token("b", TipoToken.IDENTIFICADOR, 1, 7, 7)
        ), lexer().analizar("a #if b"));
    }

    @Test
    void espaciosAntesDelNumeralSiCuentanComoInicioDeLinea() {
        assertEquals(List.of(directiva("#include <a.h>", 1, 4, 17)),
                lexer().analizar("   #include <a.h>"));
    }

    @Test
    void espaciosEntreNumeralYNombre() {
        assertEquals(List.of(directiva("#  define X", 1, 1, 11)),
                lexer().analizar("#  define X"));
    }



    @Test
    void nombreQueNoEsDirectivaQuedaComoNumeralEIdentificador() {
        // #includes  ->  #, includes      #x  ->  #, x
        assertEquals(List.of(
                new Token("#", TipoToken.SIMBOLO, 1, 1, 1),
                new Token("includes", TipoToken.IDENTIFICADOR, 1, 2, 9),
                new Token("#", TipoToken.SIMBOLO, 2, 1, 1),
                new Token("x", TipoToken.IDENTIFICADOR, 2, 2, 2)
        ), lexer().analizar("#includes\n#x"));
    }

    @Test
    void puedeIniciarRechazaLoQueNoEsDirectiva() {
        ReconocedorDirectiva reconocedor = new ReconocedorDirectiva();
        assertFalse(reconocedor.puedeIniciar(new Cursor("#hola")));      // no es directiva de C
        assertFalse(reconocedor.puedeIniciar(new Cursor("#includes")));  // el nombre sigue con letras
        assertFalse(reconocedor.puedeIniciar(new Cursor("#define2")));   // el nombre sigue con un dígito
        assertFalse(reconocedor.puedeIniciar(new Cursor("#")));          // # solo
        assertFalse(reconocedor.puedeIniciar(new Cursor("include")));    // falta el #
        assertFalse(reconocedor.puedeIniciar(new Cursor("")));
    }

    @Test
    void puedeIniciarRechazaNumeralAMitadDeLinea() {
        Cursor cursor = new Cursor("a #if b");
        cursor.avanzar();
        cursor.avanzar();   // el cursor queda en el #
        assertFalse(new ReconocedorDirectiva().puedeIniciar(cursor));
    }



    @Test
    void directivaAlFinalDelArchivo() {
        assertEquals(List.of(directiva("#else", 1, 1, 5)), lexer().analizar("#else"));
    }

    @Test
    void puedeIniciarNoAvanzaElCursor() {
        Cursor cursor = new Cursor("#define X");
        assertTrue(new ReconocedorDirectiva().puedeIniciar(cursor));
        assertEquals(1, cursor.columna());
    }

    @Test
    void leerSeDetieneAntesDelSaltoDeLinea() {
        Cursor cursor = new Cursor("#endif\nx");
        Token token = new ReconocedorDirectiva().leer(cursor);
        assertEquals(directiva("#endif", 1, 1, 6), token);
        assertEquals('\n', cursor.actual());
    }



    @Test
    void lexerPorDefectoUsaLosReconocedoresEnOrden() {
        // Directiva antes que símbolo (#) y símbolo antes que operador (...),
        // junto con los reconocedores de caracteres e identificadores que ya están en main.
        List<Token> tokens = new Lexer().analizar("#include <stdio.h>\nint f(...) <<= '\\n'");
        assertEquals(List.of(
                directiva("#include <stdio.h>", 1, 1, 18),
                new Token("int", TipoToken.PALABRA_RESERVADA, 2, 1, 3),
                new Token("f", TipoToken.IDENTIFICADOR, 2, 5, 5),
                new Token("(", TipoToken.SIMBOLO, 2, 6, 6),
                new Token("...", TipoToken.SIMBOLO, 2, 7, 9),
                new Token(")", TipoToken.SIMBOLO, 2, 10, 10),
                new Token("<<=", TipoToken.OPERADOR, 2, 12, 14),
                new Token("'\\n'", TipoToken.CONSTANTE_CARACTER, 2, 16, 19)
        ), tokens);
    }
}
