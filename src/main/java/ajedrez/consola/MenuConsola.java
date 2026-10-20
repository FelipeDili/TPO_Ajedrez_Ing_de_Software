package ajedrez.consola;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Menú inicial: deja elegir el tema de la consola antes de empezar. Es parte del adaptador;
 * el núcleo no sabe que existe.
 */
public class MenuConsola {

    private final BufferedReader teclado;
    private final PrintStream salida;
    private Tema tema;

    public MenuConsola(BufferedReader teclado, PrintStream salida, Tema temaInicial) {
        this.teclado = teclado;
        this.salida = salida;
        this.tema = temaInicial;
    }

    public Tema tema() {
        return tema;
    }

    /** Muestra el menú hasta que el usuario empieza una partida (true) o sale (false). */
    public boolean elegir() throws IOException {
        while (true) {
            salida.println();
            salida.println("=== Ajedrez ===");
            salida.println("  1. Empezar partida");
            salida.println("  2. Tema: " + (tema == Tema.UNICODE_COLOR ? "colores" : "ASCII"));
            salida.println("  0. Salir");
            String opcion = leer("Elegí una opción > ");
            if (opcion == null) {
                return false;
            }
            switch (opcion) {
                case "1" -> {
                    return true;
                }
                case "2" -> tema = tema == Tema.UNICODE_COLOR ? Tema.ASCII : Tema.UNICODE_COLOR;
                case "0", "salir" -> {
                    return false;
                }
                default -> salida.println("Opción inválida: '" + opcion + "'.");
            }
        }
    }

    /** Lee una opción no vacía; devuelve null si se cierra la entrada. */
    private String leer(String pregunta) throws IOException {
        while (true) {
            salida.print(pregunta);
            salida.flush();
            String linea = teclado.readLine();
            if (linea == null) {
                return null;
            }
            linea = linea.trim().toLowerCase();
            if (!linea.isEmpty()) {
                return linea;
            }
        }
    }
}
