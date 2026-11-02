package ajedrez.consola;

import ajedrez.core.fabrica.Promocion;
import ajedrez.core.modelo.Posicion;
import ajedrez.core.partida.EntradaAjedrez;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adaptador de entrada: traduce lo que escribe el jugador a pedidos al núcleo, que es quien decide
 * si una jugada es legal.
 */
public class PartidaConsola {

    private static final Pattern JUGADA = Pattern.compile("^([a-z]\\d{1,2})\\s*([a-z]\\d{1,2})$");
    private static final Pattern CASILLA = Pattern.compile("^[a-z]\\d{1,2}$");
    private static final String AYUDA = """
            Comandos:
              mover     te pide la pieza a mover y después el destino
              e2 e4     forma corta: origen y destino en una línea (o e2e4)
              e1 g1     enroque: mové el rey dos casillas hacia la torre
              deshacer  vuelve atrás la última jugada
              rehacer   vuelve a jugar la última jugada deshecha
              salir     abandona la partida y vuelve al menú""";

    private final BufferedReader teclado;
    private final PrintStream salida;
    private final EntradaAjedrez partida;
    private final ConsolaUI ui;

    public PartidaConsola(BufferedReader teclado, PrintStream salida, EntradaAjedrez partida, ConsolaUI ui) {
        this.teclado = teclado;
        this.salida = salida;
        this.partida = partida;
        this.ui = ui;
    }

    /** Devuelve false si se cerró la entrada y hay que terminar el programa. */
    public boolean jugar() throws IOException {
        salida.println("Comandos: mover, deshacer, rehacer, ayuda, salir (o jugá directo 'e2 e4').");
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
            } else if (linea.equals("rehacer")) {
                partida.rehacerJugada();
            } else if (linea.equals("ayuda")) {
                salida.println(AYUDA);
            } else if (linea.equals("mover")) {
                procesarMover();
            } else {
                procesarJugada(linea);
            }
        }
    }

    /** Con la partida terminada ya no hay turno: solo se puede deshacer o salir. */
    String prompt() {
        if (partida.estaTerminada()) {
            return "[terminada: deshacer o salir] > ";
        }
        return "[" + partida.turnoActual().name().toLowerCase() + "] > ";
    }

    /** Jugada guiada: avisa si el origen no sirve antes de pedir el destino. */
    private void procesarMover() throws IOException {
        if (partida.estaTerminada()) {
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
     * Devuelve null si el usuario cancela con Enter, se cierra la entrada o no escribió una
     * casilla.
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
