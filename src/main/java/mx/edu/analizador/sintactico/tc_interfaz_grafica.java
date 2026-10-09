
package mx.edu.analizador.sintactico;

import mx.edu.analizador.LectorC;
import mx.edu.analizador.lexico.Lexer;
import mx.edu.analizador.lexico.Token;
import mx.edu.analizador.lexico.ReconocedorSimbolo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class tc_interfaz_grafica extends JFrame {

    private JComboBox<String> comboArchivos;
    private JTable tabla;
    private DefaultTableModel modelo;
    private JTextArea areaCodigo;

    public tc_interfaz_grafica() {

        setTitle("Analizador Léxico - Tabla de Tokens");
        setSize(1100, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        String[] opciones = {
                "Selecciona un archivo .c",
                "prueba.c"
        };
        comboArchivos = new JComboBox<>(opciones);
        comboArchivos.setPreferredSize(new Dimension(450, 28));
        comboArchivos.setMaximumRowCount(12);
        comboArchivos.setEditable(true);

        JPanel panelSuperior = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 10, 8)
        );

        panelSuperior.add(comboArchivos);
        add(panelSuperior, BorderLayout.NORTH);

        // Tabla de tokens
        String[] columnas = {
                "Lexema", "Tipo", "Línea", "Inicio", "Fin"
        };

        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        tabla.setRowHeight(22);
        tabla.setAutoCreateRowSorter(true);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(60);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(60);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(60);

        JScrollPane scrollTabla = new JScrollPane(tabla);

        // Área donde se muestra el código C
        areaCodigo = new JTextArea();
        areaCodigo.setEditable(false);
        areaCodigo.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JScrollPane scrollCodigo = new JScrollPane(areaCodigo);

        // Divisor horizontal
        JSplitPane divisor = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                scrollTabla,
                scrollCodigo
        );

        divisor.setDividerLocation(550);
        divisor.setResizeWeight(0.5);

        add(divisor, BorderLayout.CENTER);

        // Actualizar tabla y código al seleccionar un archivo
        comboArchivos.addActionListener(e -> {

            Object seleccionado = comboArchivos.getSelectedItem();

            if (seleccionado != null) {
                mostrarArchivo(seleccionado.toString().trim());
            }

        });
    }

    // Método para mostrar el archivo seleccionado
    private void mostrarArchivo(String opcion) {

        modelo.setRowCount(0);
        areaCodigo.setText("");

        if (opcion.isEmpty() ||
                opcion.equals("Selecciona un archivo .c")) {
            return;
        }

        if (!opcion.toLowerCase(Locale.ROOT).endsWith(".c")) {
            JOptionPane.showMessageDialog(
                    this,
                    "Solo se permiten archivos .c"
            );
            return;
        }

        try {

            // Leer el archivo C
            String codigo = LectorC.Lectura(opcion);
            // Mostrar el código
            areaCodigo.setText(codigo);
            areaCodigo.setCaretPosition(0);
            // Crear el analizador léxico
            Lexer lexer = new Lexer(
                    List.of(new ReconocedorSimbolo())
            );
            // Obtener los tokens
            List<Token> tokens = lexer.analizar(codigo);
            // Agregar los tokens a la tabla
            for (Token token : tokens) {
                modelo.addRow(new Object[]{
                        token.lexema(),
                        token.tipo(),
                        token.linea(),
                        token.inicio(),
                        token.fin()
                });
            }
        } catch (IOException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error al leer el archivo: " + e.getMessage()
            );

        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error lexico: " + e.getMessage()
            );
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new tc_interfaz_grafica().setVisible(true);
        });

    }
}
