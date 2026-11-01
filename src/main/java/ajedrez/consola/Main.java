package ajedrez.consola;

import ajedrez.core.fabrica.TableroEstandarFactory;
import ajedrez.core.partida.Partida;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Punto de entrada y "composition root": acá se eligen las implementaciones concretas
 * (consola, tablero estándar) y se inyectan en el núcleo. Además de eso solo prepara la consola.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws IOException {
        ponerConsolaWindowsEnUtf8();
        PrintStream salida = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        BufferedReader teclado = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8));
        Tema temaInicial = Arrays.asList(args).contains("--ascii") ? Tema.ASCII : Tema.UNICODE_COLOR;
        MenuConsola menu = new MenuConsola(teclado, salida, temaInicial);

        while (menu.elegir()) {
            ConsolaUI ui = new ConsolaUI(salida, menu.tema());
            Partida partida = new Partida(ui, new TableroEstandarFactory());
            if (!new PartidaConsola(teclado, salida, partida, ui).jugar()) {
                break;
            }
        }
        salida.println("Hasta la próxima.");
    }

    /**
     * La consola de Windows arranca en una página de códigos vieja (850/437) y muestra los
     * símbolos de las piezas como "ÔÖ£". Equivale a correr "chcp 65001" antes de jugar.
     * Si falla, se sigue igual: queda el tema ASCII como alternativa.
     */
    private static void ponerConsolaWindowsEnUtf8() {
        if (!System.getProperty("os.name", "").toLowerCase().startsWith("windows")) {
            return;
        }
        try {
            // inheritIO: chcp tiene que actuar sobre esta misma consola; ">nul" oculta su mensaje.
            new ProcessBuilder("cmd", "/c", "chcp 65001 >nul")
                    .inheritIO()
                    .start()
                    .waitFor();
        } catch (IOException e) {
            // sin cmd disponible: se dibuja igual
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
