package mx.edu.analizador;

import mx.edu.analizador.interfaz.VentanaPrincipal;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;

public class Main {
    public static void main(String[] args) {
        // Sin pantalla (por ejemplo, al correr los tests) no se puede abrir la ventana
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Analizador Semántico: se requiere un entorno gráfico para abrir la ventana");
            return;
        }
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
