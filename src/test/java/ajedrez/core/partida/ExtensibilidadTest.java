package ajedrez.core.partida;

import ajedrez.core.fabrica.PiezaFactory;
import ajedrez.core.modelo.Color;
import ajedrez.core.modelo.MovimientoEstrategia;
import ajedrez.core.modelo.Pieza;
import ajedrez.core.modelo.Posicion;
import ajedrez.core.modelo.Tablero;
import ajedrez.core.movimiento.MovimientoCaballo;
import ajedrez.core.movimiento.MovimientoCompuesto;
import ajedrez.core.movimiento.MovimientoTorre;
import ajedrez.testsupport.NotificadorEspia;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static ajedrez.testsupport.Tableros.conjunto;
import static ajedrez.testsupport.Tableros.factory;
import static ajedrez.testsupport.Tableros.movimientos;
import static ajedrez.testsupport.Tableros.poner;
import static ajedrez.testsupport.Tableros.vacio;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Simula lo que pide la defensa individual: agregar una pieza nueva o cambiar el tamaño del
 * tablero SIN modificar ninguna clase existente del núcleo.
 */
class ExtensibilidadTest {

    /** Pieza inventada: salta 3 en una dirección y 1 en la otra. Es solo una lambda. */
    private static final MovimientoEstrategia CAMELLO = (origen, tablero) -> {
        int[][] saltos = {{3, 1}, {3, -1}, {-3, 1}, {-3, -1}, {1, 3}, {1, -3}, {-1, 3}, {-1, -3}};
        List<Posicion> resultado = new ArrayList<>();
        for (int[] s : saltos) {
            Posicion destino = origen.desplazar(s[0], s[1]);
            if (tablero.contiene(destino)) {
                resultado.add(destino);
            }
        }
        return resultado;
    };

    private final NotificadorEspia espia = new NotificadorEspia();

    @Test
    void unaPiezaNuevaSeAgregaImplementandoSoloLaEstrategia() {
        Tablero t = vacio();
        poner(t, "a1", new Pieza(Color.BLANCAS, 'M', false, CAMELLO));
        assertEquals(conjunto("b4", "d2"), movimientos(t, "a1"));
    }

    /** Partida con los dos reyes y un camello blanco en a1. */
    private Partida partidaConCamello(Tablero t) {
        poner(t, "e1", PiezaFactory.rey(Color.BLANCAS));
        poner(t, "e8", PiezaFactory.rey(Color.NEGRAS));
        poner(t, "a1", new Pieza(Color.BLANCAS, 'M', false, CAMELLO));
        return new Partida(espia, factory(t));
    }

    @Test
    void laPiezaNuevaSeMueveEnUnaPartidaConLasReglasDelNucleo() {
        Tablero t = vacio();
        Partida partida = partidaConCamello(t);

        partida.intentarMovimiento(Posicion.de("a1"), Posicion.de("b4"));

        assertTrue(espia.errores.isEmpty());
        assertNotNull(t.obtenerPieza(Posicion.de("b4")));
        assertNull(t.obtenerPieza(Posicion.de("a1")));
    }

    @Test
    void laPartidaRechazaUnMovimientoQueLaPiezaNuevaNoPuedeHacer() {
        Partida partida = partidaConCamello(vacio());

        partida.intentarMovimiento(Posicion.de("a1"), Posicion.de("a2")); // el camello no se mueve así

        assertEquals(1, espia.errores.size());
    }

    @Test
    void unaPiezaNuevaSePuedeArmarCombinandoEstrategiasExistentes() {
        // "Canciller": torre + caballo, sin escribir lógica de movimiento nueva.
        Tablero t = vacio();
        poner(t, "d4", new Pieza(Color.BLANCAS, 'K', false,
                new MovimientoCompuesto(new MovimientoTorre(), new MovimientoCaballo())));
        assertEquals(14 + 8, movimientos(t, "d4").size());
    }

    @Test
    void lasPiezasSeMuevenHastaElBordeDeUnTableroMasGrande() {
        Tablero grande = new Tablero(10, 10);
        poner(grande, "a1", PiezaFactory.torre(Color.BLANCAS));

        assertEquals(18, movimientos(grande, "a1").size());
    }

    /** Partida en un tablero de 6x6 con una torre blanca en b1. */
    private Partida partidaEnTableroChico() {
        return new Partida(espia, () -> {
            Tablero chico = new Tablero(6, 6);
            poner(chico, "a1", PiezaFactory.rey(Color.BLANCAS));
            poner(chico, "b1", PiezaFactory.torre(Color.BLANCAS));
            poner(chico, "f6", PiezaFactory.rey(Color.NEGRAS));
            return chico;
        });
    }

    @Test
    void enUnTableroChicoNoSePuedeSalirDelBorde() {
        Partida partida = partidaEnTableroChico();

        partida.intentarMovimiento(Posicion.de("b1"), Posicion.de("b7")); // fuera del 6x6

        assertEquals(1, espia.errores.size());
    }

    @Test
    void enUnTableroChicoLaUltimaFilaEsLaSexta() {
        Partida partida = partidaEnTableroChico();

        partida.intentarMovimiento(Posicion.de("b1"), Posicion.de("b6"));

        assertTrue(espia.errores.isEmpty());
        assertEquals(List.of(Color.NEGRAS), espia.jaques); // la torre en b6 da jaque en la fila 6
    }
}
