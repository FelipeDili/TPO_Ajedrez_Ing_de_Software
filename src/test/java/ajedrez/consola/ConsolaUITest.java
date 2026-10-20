package ajedrez.consola;

import ajedrez.core.fabrica.PiezaFactory;
import ajedrez.core.fabrica.TableroEstandarFactory;
import ajedrez.core.modelo.Color;
import ajedrez.core.modelo.Pieza;
import ajedrez.core.modelo.Tablero;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ajedrez.testsupport.Tableros.poner;
import static ajedrez.testsupport.Tableros.vacio;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** El adaptador de consola se prueba contra un buffer en memoria, sin terminal real. */
class ConsolaUITest {

    private static final Pattern CODIGO_ANSI = Pattern.compile("\u001B\\[[0-9;]*m");
    private static final Pattern FONDO = Pattern.compile("\u001B\\[48;5;(\\d+)m");

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    private ConsolaUI consola(Tema tema) {
        return new ConsolaUI(new PrintStream(buffer, true, StandardCharsets.UTF_8), tema);
    }

    private String salida() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private Tablero inicial() {
        return new TableroEstandarFactory().crearTableroInicial();
    }

    private String sinColores(String texto) {
        return CODIGO_ANSI.matcher(texto).replaceAll("");
    }

    private String lineaQueEmpiezaCon(String texto, String prefijo) {
        for (String linea : texto.split("\\R")) {
            if (linea.startsWith(prefijo)) {
                return linea;
            }
        }
        throw new AssertionError("No hay una línea que empiece con '" + prefijo + "'");
    }

    // ---------- Tema ASCII ----------

    @Test
    void elTemaAsciiNoUsaCodigosDeEscapeNiSimbolosUnicode() {
        consola(Tema.ASCII).alTableroActualizado(inicial());

        String texto = salida();
        assertFalse(texto.contains("\u001B"));
        assertTrue(texto.contains(" 1 T C A D R A C T "));
        assertTrue(texto.contains(" 8 t c a d r a c t "));
        assertTrue(texto.contains("   a b c d e f g h "));
    }

    @Test
    void elTemaAsciiMuestraLosMensajesSinFormato() {
        consola(Tema.ASCII).alError("algo salió mal");

        assertEquals("Error: algo salió mal" + System.lineSeparator(), salida());
    }

    // ---------- Tema Unicode + color ----------

    @Test
    void elTemaUnicodeDibujaLasPiezasConSimbolosDeAjedrez() {
        consola(Tema.UNICODE_COLOR).alTableroActualizado(inicial());

        String texto = salida();
        assertTrue(texto.contains("♜")); // torre
        assertTrue(texto.contains("♞")); // caballo
        assertTrue(texto.contains("♝")); // alfil
        assertTrue(texto.contains("♛")); // dama
        assertTrue(texto.contains("♚")); // rey
        assertTrue(texto.contains("♙")); // peón
        assertFalse(texto.contains("T C A D"));
    }

    @Test
    void cadaFilaTieneEtiquetaYOchoCasillasDeTresColumnas() {
        consola(Tema.UNICODE_COLOR).alTableroActualizado(inicial());

        String texto = sinColores(salida());
        assertEquals(" 1  ♜  ♞  ♝  ♛  ♚  ♝  ♞  ♜ ",
                lineaQueEmpiezaCon(texto, " 1 "));
        assertEquals(" 4 " + " ".repeat(8 * 3), lineaQueEmpiezaCon(texto, " 4 ")); // fila vacía
        assertTrue(texto.contains("    a  b  c  d  e  f  g  h "));
    }

    @Test
    void lasCasillasAlternanDeColorYA1EsOscura() {
        consola(Tema.UNICODE_COLOR).alTableroActualizado(inicial());

        String filaUno = lineaQueEmpiezaCon(salida(), " 1 ");
        List<String> fondos = new ArrayList<>();
        Matcher m = FONDO.matcher(filaUno);
        while (m.find()) {
            fondos.add(m.group(1));
        }
        assertEquals(List.of("94", "137", "94", "137", "94", "137", "94", "137"), fondos);
    }

    @Test
    void lasPiezasBlancasYNegrasSeDistinguenPorColor() {
        Tablero t = vacio();
        poner(t, "a1", PiezaFactory.torre(Color.BLANCAS));
        poner(t, "a8", PiezaFactory.torre(Color.NEGRAS));
        consola(Tema.UNICODE_COLOR).alTableroActualizado(t);

        String texto = salida();
        assertTrue(texto.contains("\u001B[1;38;5;231m")); // blancas: blanco brillante
        assertTrue(texto.contains("\u001B[38;5;16m"));    // negras: negro
    }

    @Test
    void unaPiezaSinSimboloConocidoSeDibujaConSuLetra() {
        Tablero t = vacio();
        poner(t, "d4", new Pieza(Color.BLANCAS, 'K', false, (origen, tablero) -> List.of()));
        consola(Tema.UNICODE_COLOR).alTableroActualizado(t);

        assertTrue(sinColores(salida()).contains(" K "));
    }

    @Test
    void losErroresSePintanDeRojo() {
        consola(Tema.UNICODE_COLOR).alError("x");

        assertTrue(salida().contains("\u001B[31mError: x\u001B[0m"));
    }

    @Test
    void elJaqueSePintaDeAmarillo() {
        consola(Tema.UNICODE_COLOR).alJaque(Color.BLANCAS);

        assertTrue(salida().contains("\u001B[1;33m¡Jaque a las blancas!\u001B[0m"));
    }

    @Test
    void elFinDePartidaSePintaDeCeleste() {
        consola(Tema.UNICODE_COLOR).alFinDePartida("tablas");

        assertTrue(salida().contains("\u001B[1;36m*** Fin de la partida: tablas ***\u001B[0m"));
    }
}
