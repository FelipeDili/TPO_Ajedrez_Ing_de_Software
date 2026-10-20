package ajedrez.consola;

import ajedrez.core.fabrica.PiezaFactory;
import ajedrez.core.fabrica.Promocion;
import ajedrez.core.fabrica.TableroEstandarFactory;
import ajedrez.core.fabrica.TableroFactory;
import ajedrez.core.modelo.Color;
import ajedrez.core.modelo.Posicion;
import ajedrez.core.modelo.Tablero;
import ajedrez.core.partida.Partida;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static ajedrez.testsupport.Tableros.factory;
import static ajedrez.testsupport.Tableros.poner;
import static ajedrez.testsupport.Tableros.vacio;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** La consola de la partida se maneja con una entrada armada en memoria, sin terminal real. */
class PartidaConsolaTest {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private Partida partida;

    private PartidaConsola consola(TableroFactory tableroInicial, String entrada) {
        PrintStream salida = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        ConsolaUI ui = new ConsolaUI(salida, Tema.ASCII);
        partida = new Partida(ui, tableroInicial);
        return new PartidaConsola(new BufferedReader(new StringReader(entrada)), salida, partida, ui);
    }

    private PartidaConsola consolaEstandar() {
        return consola(new TableroEstandarFactory(), "");
    }

    private String salida() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private void jugar(String origen, String destino) {
        partida.intentarMovimiento(Posicion.de(origen), Posicion.de(destino));
    }

    private void mateDelLoco() {
        jugar("f2", "f3");
        jugar("e7", "e5");
        jugar("g2", "g4");
        jugar("d8", "h4");
    }

    // ---------- Prompt ----------

    @Test
    void alEmpezarElPromptMuestraElTurnoDeLasBlancas() {
        PartidaConsola consola = consolaEstandar();

        String prompt = consola.prompt();

        assertEquals("[blancas] > ", prompt);
    }

    @Test
    void despuesDeUnaJugadaElPromptMuestraElTurnoDeLasNegras() {
        PartidaConsola consola = consolaEstandar();
        jugar("e2", "e4");

        String prompt = consola.prompt();

        assertEquals("[negras] > ", prompt);
    }

    @Test
    void conLaPartidaTerminadaElPromptNoOfreceTurno() {
        PartidaConsola consola = consolaEstandar();
        mateDelLoco();

        String prompt = consola.prompt();

        assertEquals("[terminada: deshacer o salir] > ", prompt);
    }

    @Test
    void alDeshacerElMateElPromptVuelveAMostrarElTurno() {
        PartidaConsola consola = consolaEstandar();
        mateDelLoco();

        partida.deshacerJugada();

        assertEquals("[negras] > ", consola.prompt());
    }

    // ---------- Promoción ----------

    @ParameterizedTest
    @CsvSource({"d, DAMA", "reina, DAMA", "t, TORRE", "alfil, ALFIL", "c, CABALLO"})
    void entiendeLaLetraONombreDeLaPiezaParaPromocionar(String respuesta, Promocion esperada) {
        Promocion promocion = PartidaConsola.promocionDeLetra(respuesta);

        assertEquals(esperada, promocion);
    }

    @ParameterizedTest
    @ValueSource(strings = {"r", "rey", "x"})
    void noAceptaPiezasQueNoSonDePromocion(String respuesta) {
        Promocion promocion = PartidaConsola.promocionDeLetra(respuesta);

        assertNull(promocion);
    }

    @Test
    void moverHastaLaUltimaFilaPreguntaYUsaLaPiezaElegida() throws IOException {
        Tablero tablero = vacio();
        poner(tablero, "e1", PiezaFactory.rey(Color.BLANCAS));
        poner(tablero, "h5", PiezaFactory.rey(Color.NEGRAS));
        poner(tablero, "a7", PiezaFactory.peon(Color.BLANCAS));
        PartidaConsola consola = consola(factory(tablero), "mover\na7\na8\nc\nsalir\n");

        consola.jugar();

        assertEquals('C', tablero.obtenerPieza(Posicion.de("a8")).representacion());
        assertTrue(salida().contains("¿En qué pieza lo convertís?"));
    }

    // ---------- Comando mover ----------

    @Test
    void moverDesdeUnaPiezaRivalAvisaSinPedirElDestino() throws IOException {
        PartidaConsola consola = consola(new TableroEstandarFactory(), "mover\ne7\nsalir\n");

        consola.jugar();

        assertTrue(salida().contains("No es tu turno"));
        assertFalse(salida().contains("¿A dónde?"));
    }
}
