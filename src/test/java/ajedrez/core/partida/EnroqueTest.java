package ajedrez.core.partida;

import ajedrez.core.fabrica.PiezaFactory;
import ajedrez.core.fabrica.TableroEstandarFactory;
import ajedrez.core.modelo.Color;
import ajedrez.core.modelo.Pieza;
import ajedrez.core.modelo.Posicion;
import ajedrez.core.modelo.Tablero;
import ajedrez.testsupport.NotificadorEspia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ajedrez.testsupport.Tableros.factory;
import static ajedrez.testsupport.Tableros.poner;
import static ajedrez.testsupport.Tableros.vacio;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * El rey enroca moviéndose dos casillas hacia una torre; la torre salta a su lado.
 * Se arma un tablero con el rey blanco en e1 y las dos torres blancas en sus esquinas.
 */
class EnroqueTest {

    private NotificadorEspia espia;
    private Tablero tablero;
    private Pieza rey;
    private Pieza torreCorta;
    private Pieza torreLarga;

    @BeforeEach
    void reyYTorresEnSuLugar() {
        espia = new NotificadorEspia();
        tablero = vacio();
        rey = poner(tablero, "e1", PiezaFactory.rey(Color.BLANCAS));
        torreCorta = poner(tablero, "h1", PiezaFactory.torre(Color.BLANCAS));
        torreLarga = poner(tablero, "a1", PiezaFactory.torre(Color.BLANCAS));
        poner(tablero, "e8", PiezaFactory.rey(Color.NEGRAS));
    }

    private Partida partida() {
        return new Partida(espia, factory(tablero));
    }

    private void jugar(Partida p, String origen, String destino) {
        p.intentarMovimiento(Posicion.de(origen), Posicion.de(destino));
    }

    private Pieza en(String casilla) {
        return tablero.obtenerPieza(Posicion.de(casilla));
    }

    @Test
    void enroqueCortoMueveElReyYLaTorre() {
        Partida p = partida();

        jugar(p, "e1", "g1");

        assertSame(rey, en("g1"));
        assertSame(torreCorta, en("f1"));
        assertNull(en("e1"));
        assertNull(en("h1"));
        assertTrue(espia.errores.isEmpty());
    }

    @Test
    void enroqueLargoMueveElReyYLaTorre() {
        Partida p = partida();

        jugar(p, "e1", "c1");

        assertSame(rey, en("c1"));
        assertSame(torreLarga, en("d1"));
        assertNull(en("a1"));
        assertTrue(espia.errores.isEmpty());
    }

    @Test
    void noSePuedeEnrocarSiElReyYaSeMovio() {
        rey.registrarMovimiento();
        Partida p = partida();

        jugar(p, "e1", "g1");

        assertEquals(1, espia.errores.size());
        assertSame(rey, en("e1"));
    }

    @Test
    void noSePuedeEnrocarConUnaTorreQueYaSeMovio() {
        torreCorta.registrarMovimiento();
        Partida p = partida();

        jugar(p, "e1", "g1");

        assertEquals(1, espia.errores.size());
    }

    @Test
    void noSePuedeEnrocarConPiezasEnElMedio() {
        poner(tablero, "g1", PiezaFactory.caballo(Color.BLANCAS));
        Partida p = partida();

        jugar(p, "e1", "g1");

        assertEquals(1, espia.errores.size());
    }

    @Test
    void noSePuedeEnrocarEstandoEnJaque() {
        poner(tablero, "e5", PiezaFactory.torre(Color.NEGRAS));
        Partida p = partida();

        jugar(p, "e1", "g1");

        assertEquals(1, espia.errores.size());
    }

    @Test
    void noSePuedeEnrocarPasandoPorUnaCasillaAtacada() {
        poner(tablero, "f5", PiezaFactory.torre(Color.NEGRAS)); // ataca f1
        Partida p = partida();

        jugar(p, "e1", "g1");

        assertEquals(1, espia.errores.size());
    }

    @Test
    void noSePuedeEnrocarHaciaUnaCasillaAtacada() {
        poner(tablero, "g5", PiezaFactory.torre(Color.NEGRAS)); // ataca g1
        Partida p = partida();

        jugar(p, "e1", "g1");

        assertEquals(1, espia.errores.size());
    }

    @Test
    void conUnaCasillaAtacadaDelOtroLadoSePuedeEnrocarIgual() {
        poner(tablero, "b5", PiezaFactory.torre(Color.NEGRAS)); // ataca b1: no importa al rey
        Partida p = partida();

        jugar(p, "e1", "c1");

        assertTrue(espia.errores.isEmpty());
    }

    @Test
    void deshacerElEnroqueDevuelveElReyYLaTorreSinMarcarlosComoMovidos() {
        Partida p = partida();
        jugar(p, "e1", "g1");

        p.deshacerJugada();

        assertSame(rey, en("e1"));
        assertSame(torreCorta, en("h1"));
        assertNull(en("g1"));
        assertNull(en("f1"));
        assertFalse(rey.seHaMovido());
        assertFalse(torreCorta.seHaMovido());
    }

    @Test
    void sePuedeEnrocarEnUnaPartidaEstandarDespuesDeAbrirLaFila() {
        Partida p = new Partida(espia, new TableroEstandarFactory());
        jugar(p, "e2", "e4");
        jugar(p, "e7", "e5");
        jugar(p, "g1", "f3");
        jugar(p, "b8", "c6");
        jugar(p, "f1", "c4");
        jugar(p, "g8", "f6");

        jugar(p, "e1", "g1");

        assertTrue(espia.errores.isEmpty());
        assertEquals('R', p.tablero().obtenerPieza(Posicion.de("g1")).representacion());
        assertEquals('T', p.tablero().obtenerPieza(Posicion.de("f1")).representacion());
    }
}
