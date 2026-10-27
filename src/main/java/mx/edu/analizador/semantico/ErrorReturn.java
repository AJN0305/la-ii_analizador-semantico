package mx.edu.analizador.semantico;

import mx.edu.analizador.lexico.TipoToken;
import mx.edu.analizador.lexico.Token;

import java.util.ArrayList;
import java.util.List;

public class ErrorReturn {

    //Tipos de numeros en C
    private static final List<String> TIPOS_NUMEROS = List.of("int", "float", "double", "char", "short", "long", "signed", "unsigned");

    public List<ErrorSemantico> analizar(List<Token> tokens) {
        List<ErrorSemantico> errores = new ArrayList<>();

        //True si la llave es de una funcion, False si es un if, for, entre otros
        List<Boolean> llaves = new ArrayList<>();

        //Lo que devuelve la funcion: void, numero, puntero u otro. Si no esta dentro de ninguna funcion, vale null
        String tipoFuncion = null;
        String nombreFuncion = "";

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);

            if (esSimbolo(token, "{")) {
                boolean esFuncion = false;

                //La funcion debe de verse asi: tipo nombre(...) {
                if (tipoFuncion == null && i > 0 && esSimbolo(tokens.get(i - 1), ")")) {
                    //Se regresa en la funcion hasta encontrar (
                    int j = i - 1;
                    while (j > 0 && !esSimbolo(tokens.get(j), "(")) {
                        j--;
                    }
                    //Verifica que lo que esta antes de ( es el nombre de la funcion
                    if (j > 0 && tokens.get(j - 1).tipo() == TipoToken.IDENTIFICADOR) {
                        esFuncion = true;
                        nombreFuncion = tokens.get(j - 1).lexema();
                        tipoFuncion = obtenerTipo(tokens, j - 1);
                    }
                }
                llaves.add(esFuncion);
            }
            else if (esSimbolo(token, "}")) {
                if (llaves.size() > 0) {
                    boolean eraFuncion = llaves.remove(llaves.size() - 1);
                    if (eraFuncion) {
                        tipoFuncion = null; //Significa que esta afuera de la funcion
                    }
                }
            }
            else if (esPalabra(token, "return") && tipoFuncion != null && i + 1 < tokens.size()) {
                Token siguiente = tokens.get(i + 1);

                if (esSimbolo(siguiente, ";")) {
                    //return; (es decir, sin valor)
                    if (tipoFuncion.equals("numero") || tipoFuncion.equals("puntero")) {
                        errores.add(crearError(token, "La funcion: " + nombreFuncion + " debe de devolver un valor"));
                    }
                }
                else if (tipoFuncion.equals("void")) {
                    //si return quiere devolver un valor en una funcion void
                    errores.add(crearError(token, "La funcion: " + nombreFuncion + " es void, por lo que no puede devolver valores"));
                }
                else if (i + 2 < tokens.size() && esSimbolo(tokens.get(i + 2), ";")) {
                    //Si se usa return con un solo valor de forma directa
                    if (tipoFuncion.equals("numero") && siguiente.tipo() == TipoToken.CADENA) {
                        errores.add(crearError(token, "La funcion: " + nombreFuncion + " debe de devolver un numero, no una cadena"));
                    }
                    else if (tipoFuncion.equals("puntero") && esNumero(siguiente) && !siguiente.lexema().equals("0")) {
                        //se deja el 0 porque es NULL
                        errores.add(crearError(token, "La funcion: " + nombreFuncion + " debe de devolver un apuntador, no un numero"));
                    }
                }
            }
        }
        return errores;
    }

    //Investiga que hay antes del nombre de la funcion para saber que devolvera
    private String obtenerTipo(List<Token> tokens, int nombre) {
        int i = nombre - 1;
        boolean puntero = false;

        //si es char *nombre()
        while (i >= 0 && esOperador(tokens.get(i), "*")) {
            puntero = true;
            i--;
        }
        if (puntero) {
            return "puntero";
        }
        if (i < 0) {
            return "otro";
        }

        Token tipo = tokens.get(i);
        if (esPalabra(tipo, "void")) {
            return "void";
        }
        if (tipo.tipo() == TipoToken.PALABRA_RESERVADA && TIPOS_NUMEROS.contains(tipo.lexema())) {
            return "numero";
        }
        // struct, typedef, o si no sabemos que es, mejor no marcar nada
        return "otro";
    }

    private ErrorSemantico crearError(Token token, String descripcion) {
        return new ErrorSemantico("Uso incorrecto de return", descripcion,
                token.linea(), token.inicio(), token.fin());
    }

    private boolean esNumero(Token token) {
        return token.tipo() == TipoToken.CONSTANTE_ENTERA
                || token.tipo() == TipoToken.CONSTANTE_REAL
                || token.tipo() == TipoToken.CONSTANTE_CARACTER;
    }

    // para no escribir token.tipo() ==  en cada if
    private boolean esPalabra(Token token, String lexema) {
        return token.tipo() == TipoToken.PALABRA_RESERVADA && token.lexema().equals(lexema);
    }

    private boolean esSimbolo(Token token, String lexema) {
        return token.tipo() == TipoToken.SIMBOLO && token.lexema().equals(lexema);
    }

    private boolean esOperador(Token token, String lexema) {
        return token.tipo() == TipoToken.OPERADOR && token.lexema().equals(lexema);
    }
}
      
  
