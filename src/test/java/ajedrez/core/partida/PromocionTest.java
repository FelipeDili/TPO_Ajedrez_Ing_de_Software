package ajedrez.core.partida;

import ajedrez.core.fabrica.PiezaFactory;
import ajedrez.core.fabrica.Promocion;
import ajedrez.core.modelo.Color;
import ajedrez.core.modelo.Pieza;
import ajedrez.core.modelo.Posicion;
import ajedrez.core.modelo.Tablero;
import ajedrez.testsupport.NotificadorEspia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static ajedrez.testsupport.Tableros.factory;
import static ajedrez.testsupport.Tableros.poner;
import static ajedrez.testsupport.Tableros.vacio;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Un peón que llega a la última fila se convierte en la pieza que elige el jugador. */
class PromocionTest {

    private NotificadorEspia espia;
    private Tablero tablero;

    @BeforeEach
    void tableroConReyes() {
        espia = new NotificadorEspia();
        tablero = vacio();
        poner(tablero, "e1", PiezaFactory.rey(Color.BLANCAS));
        poner(tablero, "h5", PiezaFactory.rey(Color.NEGRAS));
    }

    private Partida partida() {
        return new Partida(espia, factory(tablero));
    }

    private void jugar(Partida p, String origen, String destino, Promocion promocion) {
        p.intentarMovimiento(Posicion.de(origen), Posicion.de(destino), promocion);
    }

    private char en(String casilla) {
        Pieza pieza = tablero.obtenerPieza(Posicion.de(casilla));
        return pieza == null ? '.' : pieza.representacion();
    }

    @ParameterizedTest
    @EnumSource(Promocion.class)
    void elPeonSeConvierteEnLaPiezaElegida(Promocion promocion) {
        poner(tablero, "a7", PiezaFactory.peon(Color.BLANCAS));

        jugar(partida(), "a7", "a8", promocion);

        assertEquals(promocion.crear(Color.BLANCAS).representacion(), en("a8"));
        assertEquals('.', en("a7"));
        assertTrue(espia.errores.isEmpty());
    }

    @Test
    void sinElegirPiezaElPeonSeConvierteEnDama() {
        poner(tablero, "a7", PiezaFactory.peon(Color.BLANCAS));

        partida().intentarMovimiento(Posicion.de("a7"), Posicion.de("a8"));

        assertEquals('D', en("a8"));
    }

    @Test
    void elPeonNegroPromocionaEnLaPrimeraFila() {
        poner(tablero, "h2", PiezaFactory.peon(Color.NEGRAS));
        Partida p = partida();
        jugar(p, "e1", "d1", Promocion.DAMA); // juegan las blancas

        jugar(p, "h2", "h1", Promocion.TORRE);

        assertEquals('t', en("h1"));
        assertTrue(espia.errores.isEmpty());
    }

    @Test
    void sePuedePromocionarCapturando() {
        poner(tablero, "a7", PiezaFactory.peon(Color.BLANCAS));
        poner(tablero, "b8", PiezaFactory.torre(Color.NEGRAS));

        jugar(partida(), "a7", "b8", Promocion.ALFIL);

        assertEquals('A', en("b8"));
        assertEquals('.', en("a7"));
    }

    @Test
    void deshacerDevuelveElMismoPeonYLaPiezaCapturada() {
        Pieza peon = poner(tablero, "a7", PiezaFactory.peon(Color.BLANCAS));
        Pieza torre = poner(tablero, "b8", PiezaFactory.torre(Color.NEGRAS));
        Partida p = partida();

        jugar(p, "a7", "b8", Promocion.CABALLO);
        p.deshacerJugada();

        assertSame(peon, tablero.obtenerPieza(Posicion.de("a7")));
        assertSame(torre, tablero.obtenerPieza(Posicion.de("b8")));
        assertFalse(peon.seHaMovido());
        assertEquals(Color.BLANCAS, p.turnoActual());
    }

    @Test
    void unaPromocionPuedeDarJaqueMate() {
        tablero = vacio();
        poner(tablero, "e1", PiezaFactory.rey(Color.BLANCAS));
        poner(tablero, "h8", PiezaFactory.rey(Color.NEGRAS));
        poner(tablero, "g7", PiezaFactory.peon(Color.NEGRAS));
        poner(tablero, "h7", PiezaFactory.peon(Color.NEGRAS));
        poner(tablero, "a7", PiezaFactory.peon(Color.BLANCAS));
        Partida p = partida();

        jugar(p, "a7", "a8", Promocion.TORRE);

        JaqueMate mate = assertInstanceOf(JaqueMate.class, p.estado());
        assertEquals(Color.BLANCAS, mate.ganador());
    }

    /**
     * Rey negro en a1, encerrado por el rey blanco en c2. Una pieza nueva en g8 que controle
     * la diagonal hasta a2 deja a las negras sin jugadas y sin estar en jaque.
     */
    private Partida partidaAPuntoDeAhogar() {
        tablero = vacio();
        poner(tablero, "c2", PiezaFactory.rey(Color.BLANCAS));
        poner(tablero, "a1", PiezaFactory.rey(Color.NEGRAS));
        poner(tablero, "g7", PiezaFactory.peon(Color.BLANCAS));
        return partida();
    }

    @Test
    void promocionarADamaPuedeAhogarAlRival() {
        Partida p = partidaAPuntoDeAhogar();

        jugar(p, "g7", "g8", Promocion.DAMA);

        Tablas tablas = assertInstanceOf(Tablas.class, p.estado());
        assertEquals(MotivoTablas.AHOGADO, tablas.motivo());
    }

    @Test
    void promocionarATorreEnLaMismaPosicionEvitaElAhogado() {
        Partida p = partidaAPuntoDeAhogar();

        jugar(p, "g7", "g8", Promocion.TORRE);

        assertFalse(p.estado().estaTerminada());
    }

    @Test
    void laPromocionUsaLaUltimaFilaDeCualquierTablero() {
        tablero = new Tablero(6, 6);
        poner(tablero, "a1", PiezaFactory.rey(Color.BLANCAS));
        poner(tablero, "f3", PiezaFactory.rey(Color.NEGRAS));
        poner(tablero, "c5", PiezaFactory.peon(Color.BLANCAS));

        jugar(partida(), "c5", "c6", Promocion.DAMA);

        assertEquals('D', en("c6"));
    }

    @Test
    void requierePromocionSoloSiLaJugadaEsLegalYLlegaAlFinal() {
        poner(tablero, "a7", PiezaFactory.peon(Color.BLANCAS));
        poner(tablero, "c6", PiezaFactory.peon(Color.BLANCAS));
        poner(tablero, "b8", PiezaFactory.alfil(Color.NEGRAS));
        poner(tablero, "b7", PiezaFactory.peon(Color.BLANCAS));
        Partida p = partida();

        assertTrue(p.requierePromocion(Posicion.de("a7"), Posicion.de("a8")));
        assertTrue(p.requierePromocion(Posicion.de("a7"), Posicion.de("b8")));  // capturando
        assertFalse(p.requierePromocion(Posicion.de("c6"), Posicion.de("c7"))); // no llega al final
        assertFalse(p.requierePromocion(Posicion.de("b7"), Posicion.de("b8"))); // bloqueado
        assertFalse(p.requierePromocion(Posicion.de("e1"), Posicion.de("e2"))); // no es un peón
        assertNull(tablero.obtenerPieza(Posicion.de("a8")));                    // solo consulta
    }
}
