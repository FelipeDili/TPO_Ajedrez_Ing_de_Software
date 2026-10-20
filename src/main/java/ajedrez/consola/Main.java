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
 * (consola, tablero estándar) y se inyectan en el núcleo. No tiene lógica propia.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws IOException {
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
}
