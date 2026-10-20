package ajedrez.consola;

import ajedrez.core.fabrica.Promocion;
import ajedrez.core.modelo.Posicion;
import ajedrez.core.partida.Partida;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adaptador de entrada: lee lo que escribe el jugador durante una partida y lo traduce a
 * pedidos al núcleo. Solo interpreta texto y conversa con el jugador; si una jugada es legal
 * lo decide siempre el núcleo.
 */
public class PartidaConsola {

    private static final Pattern JUGADA = Pattern.compile("^([a-z]\\d{1,2})\\s*([a-z]\\d{1,2})$");
    private static final Pattern CASILLA = Pattern.compile("^[a-z]\\d{1,2}$");
    private static final String AYUDA = """
            Comandos:
              mover     te pide la pieza a mover y después el destino
              e2 e4     forma corta: origen y destino en una línea (o e2e4)
              deshacer  vuelve atrás la última jugada
              salir     abandona la partida y vuelve al menú""";

    private final BufferedReader teclado;
    private final PrintStream salida;
    private final Partida partida;
    private final ConsolaUI ui;

    public PartidaConsola(BufferedReader teclado, PrintStream salida, Partida partida, ConsolaUI ui) {
        this.teclado = teclado;
        this.salida = salida;
        this.partida = partida;
        this.ui = ui;
    }

    /** Juega la partida; devuelve false si se cerró la entrada y hay que terminar el programa. */
    public boolean jugar() throws IOException {
        salida.println("Comandos: mover, deshacer, ayuda, salir (o jugá directo 'e2 e4').");
        partida.iniciar();

        while (true) {
            salida.print(prompt());
            salida.flush();
            String linea = teclado.readLine();
            if (linea == null) {
                return false;
            }
            linea = linea.trim().toLowerCase();
            if (linea.isEmpty()) {
                continue;
            }
            if (linea.equals("salir")) {
                return true;
            } else if (linea.equals("deshacer")) {
                partida.deshacerJugada();
            } else if (linea.equals("ayuda")) {
                salida.println(AYUDA);
            } else if (linea.equals("mover")) {
                procesarMover();
            } else {
                procesarJugada(linea);
            }
        }
    }

    /**
     * El prompt refleja la fase (State): con la partida terminada ya no hay turno, solo
     * se puede deshacer o salir.
     */
    String prompt() {
        if (partida.estado().estaTerminada()) {
            return "[terminada: deshacer o salir] > ";
        }
        return "[" + partida.turnoActual().name().toLowerCase() + "] > ";
    }

    /**
     * Jugada guiada en dos pasos. Antes de pedir el destino, el núcleo confirma que en el
     * origen hay una pieza del jugador de turno, para no hacer escribir de más.
     */
    private void procesarMover() throws IOException {
        if (partida.estado().estaTerminada()) {
            ui.alError("La partida terminó: solo se puede deshacer o salir.");
            return;
        }
        Posicion origen = preguntarCasilla("  ¿Qué pieza movés? (ej. e2, Enter cancela) > ");
        if (origen == null) {
            return;
        }
        Optional<String> motivo = partida.motivoParaNoMoverDesde(origen);
        if (motivo.isPresent()) {
            ui.alError(motivo.get());
            return;
        }
        Posicion destino = preguntarCasilla("  ¿A dónde? (ej. e4, Enter cancela) > ");
        if (destino == null) {
            return;
        }
        enviarJugada(origen, destino);
    }

    /**
     * Pide una casilla; devuelve null si el usuario la deja vacía (cancela), se cierra la
     * entrada o lo que escribió no es una casilla (en ese caso avisa).
     */
    private Posicion preguntarCasilla(String pregunta) throws IOException {
        salida.print(pregunta);
        salida.flush();
        String respuesta = teclado.readLine();
        if (respuesta == null || respuesta.isBlank()) {
            return null;
        }
        respuesta = respuesta.trim().toLowerCase();
        if (!CASILLA.matcher(respuesta).matches()) {
            ui.alError("No entendí '" + respuesta + "'. Escribí una casilla, por ejemplo e2.");
            return null;
        }
        return Posicion.de(respuesta);
    }

    private void procesarJugada(String linea) throws IOException {
        Matcher m = JUGADA.matcher(linea);
        if (!m.matches()) {
            ui.alError("No entendí '" + linea + "'. Usá el formato 'e2 e4'.");
            return;
        }
        enviarJugada(Posicion.de(m.group(1)), Posicion.de(m.group(2)));
    }

    /** Manda la jugada al núcleo; si lleva un peón a la última fila, antes pregunta en qué convertirlo. */
    private void enviarJugada(Posicion origen, Posicion destino) throws IOException {
        Promocion promocion = Promocion.DAMA;
        if (partida.requierePromocion(origen, destino)) {
            promocion = preguntarPromocion();
        }
        partida.intentarMovimiento(origen, destino, promocion);
    }

    private Promocion preguntarPromocion() throws IOException {
        salida.println("  ¡Tu peón llega al final! ¿En qué pieza lo convertís?");
        while (true) {
            salida.print("  D = dama, T = torre, A = alfil, C = caballo (Enter = dama) > ");
            salida.flush();
            String respuesta = teclado.readLine();
            if (respuesta == null || respuesta.isBlank()) {
                return Promocion.DAMA;
            }
            Promocion elegida = promocionDeLetra(respuesta.trim().toLowerCase());
            if (elegida != null) {
                return elegida;
            }
            salida.println("  No entendí '" + respuesta.trim() + "'. Escribí D, T, A o C.");
        }
    }

    /** Traduce la respuesta del jugador ("c" o "caballo") a una promoción, o null si no se entiende. */
    static Promocion promocionDeLetra(String respuesta) {
        return switch (respuesta) {
            case "d", "dama", "reina" -> Promocion.DAMA;
            case "t", "torre" -> Promocion.TORRE;
            case "a", "alfil" -> Promocion.ALFIL;
            case "c", "caballo" -> Promocion.CABALLO;
            default -> null;
        };
    }
}
