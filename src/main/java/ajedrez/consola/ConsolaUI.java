package ajedrez.consola;

import ajedrez.core.modelo.Color;
import ajedrez.core.modelo.Pieza;
import ajedrez.core.modelo.Posicion;
import ajedrez.core.modelo.Tablero;
import ajedrez.core.partida.NotificadorUI;

import java.io.PrintStream;
import java.util.Map;

/**
 * Adaptador de salida: dibuja el tablero y los avisos en la consola. Depende del núcleo, no
 * al revés. El núcleo solo conoce letras ('P', 'T'...); acá se traducen a símbolos Unicode,
 * así que cambiar el aspecto no obliga a tocar ninguna regla del juego.
 */
public class ConsolaUI implements NotificadorUI {

    private static final String CSI = "\u001B[";
    private static final String RESET = CSI + "0m";

    // Paleta ANSI de 256 colores: tonos de madera con buen contraste para ambos bandos.
    private static final int CASILLA_CLARA = 137;
    private static final int CASILLA_OSCURA = 94;
    private static final int PIEZA_BLANCA = 231;
    private static final int PIEZA_NEGRA = 16;

    // Se usan los símbolos "rellenos" para los dos bandos y se distinguen por color.
    // Excepto el peón: Windows dibuja "♟" como emoji e ignora el color, así que va el hueco.
    private static final Map<Character, String> SIMBOLOS = Map.of(
            'P', "♙",
            'T', "♜",
            'C', "♞",
            'A', "♝",
            'D', "♛",
            'R', "♚");

    private final PrintStream salida;
    private final Tema tema;

    public ConsolaUI(PrintStream salida, Tema tema) {
        this.salida = salida;
        this.tema = tema;
    }

    @Override
    public void alTableroActualizado(Tablero tablero) {
        salida.println();
        if (tema == Tema.UNICODE_COLOR) {
            dibujarConColor(tablero);
        } else {
            dibujarAscii(tablero);
        }
        salida.println();
    }

    @Override
    public void alJaque(Color colorEnPeligro) {
        salida.println(estilo("1;33", "¡Jaque a las " + colorEnPeligro.name().toLowerCase() + "!"));
    }

    @Override
    public void alError(String mensaje) {
        salida.println(estilo("31", "Error: " + mensaje));
    }

    @Override
    public void alFinDePartida(String resultado) {
        salida.println(estilo("1;36", "*** Fin de la partida: " + resultado + " ***"));
    }

    // ---------- Dibujo ----------

    private void dibujarAscii(Tablero tablero) {
        for (int fila = tablero.filas() - 1; fila >= 0; fila--) {
            StringBuilder linea = new StringBuilder(String.format("%2d ", fila + 1));
            for (int columna = 0; columna < tablero.columnas(); columna++) {
                Pieza pieza = tablero.obtenerPieza(new Posicion(fila, columna));
                linea.append(pieza == null ? '.' : pieza.representacion()).append(' ');
            }
            salida.println(linea);
        }
        StringBuilder letras = new StringBuilder("   ");
        for (int columna = 0; columna < tablero.columnas(); columna++) {
            letras.append((char) ('a' + columna)).append(' ');
        }
        salida.println(letras);
    }

    private void dibujarConColor(Tablero tablero) {
        for (int fila = tablero.filas() - 1; fila >= 0; fila--) {
            StringBuilder linea = new StringBuilder(String.format("%2d ", fila + 1));
            for (int columna = 0; columna < tablero.columnas(); columna++) {
                Pieza pieza = tablero.obtenerPieza(new Posicion(fila, columna));
                boolean casillaClara = (fila + columna) % 2 == 1; // a1 es oscura
                linea.append(celda(pieza, casillaClara));
            }
            salida.println(linea);
        }
        StringBuilder letras = new StringBuilder("   ");
        for (int columna = 0; columna < tablero.columnas(); columna++) {
            letras.append(' ').append((char) ('a' + columna)).append(' ');
        }
        salida.println(letras);
    }

    /** Una casilla ocupa 3 columnas de texto: espacio, símbolo, espacio. */
    private String celda(Pieza pieza, boolean casillaClara) {
        String fondo = CSI + "48;5;" + (casillaClara ? CASILLA_CLARA : CASILLA_OSCURA) + "m";
        if (pieza == null) {
            return fondo + "   " + RESET;
        }
        char letra = Character.toUpperCase(pieza.representacion());
        String simbolo = SIMBOLOS.getOrDefault(letra, String.valueOf(letra)); // pieza nueva: su letra
        String frente = pieza.color() == Color.BLANCAS
                ? CSI + "1;38;5;" + PIEZA_BLANCA + "m"
                : CSI + "38;5;" + PIEZA_NEGRA + "m";
        return fondo + frente + " " + simbolo + " " + RESET;
    }

    private String estilo(String codigo, String texto) {
        return tema == Tema.UNICODE_COLOR ? CSI + codigo + "m" + texto + RESET : texto;
    }
}
