package mx.edu.analizador.semantico;

public class ErrorSemantico {
    private final String nombre;
    private final String descripcion;
    private final int linea;
    private final int inicio;
    private final int fin;

    public ErrorSemantico(String nombre, String descripcion, int linea, int inicio, int fin) {
      this.nombre = nombre;
      this.descripcion = descripcion;
      this.linea = linea;
      this.inicio = inicio;
      this.fin = fin;
    }

    public String getNombre() {

        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getLinea() {
        return linea;
    }

    public int getInicio() {
        return inicio;
    }

    public int getFin() {
        return fin;
    }

    /*@Override
    public String toString() {
        return nombre + " en línea " + linea + ", columna " + inicio;
    }*/
}