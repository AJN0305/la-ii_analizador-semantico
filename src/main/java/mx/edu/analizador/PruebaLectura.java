package mx.edu.analizador;
import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;
import java.util.List;
import mx.edu.analizador.lexico.Lexer;
import mx.edu.analizador.lexico.Token;
import mx.edu.analizador.lexico.ReconocedorSimbolo;

public class PruebaLectura {
    public static void main (String[] args) throws IOException {
        Scanner tomasa = new Scanner(System.in);
        System.out.println("Agrega la ruta del archivo: ");

        String ruta = tomasa.nextLine();
        if (ruta.toLowerCase().endsWith(".c")) {
            String c = LectorC.Lectura(ruta);
            System.out.println("EL codigo es");
            System.out.println(c);
            // Aqui se crea el lexer donde se le otorga el reconocedor de simbolos
            Lexer lexer = new Lexer(List.of(new ReconocedorSimbolo()));
            //Aqui enviamos el archivo de el codigo al lexer
            List<Token> tokens = lexer.analizar(c);
            //System.out.println("La ruta es " + ruta);
            for (Token token : tokens) {
                System.out.println(token);}
            tomasa.close();
        }else {
            System.out.println("No es un archivo c");
        }
    }
}
