package ajedrez.core.movimiento;

import ajedrez.core.fabrica.PiezaFactory;
import ajedrez.core.modelo.Color;
import ajedrez.core.modelo.Pieza;
import ajedrez.core.modelo.Posicion;
import ajedrez.core.modelo.Tablero;
import org.junit.jupiter.api.Test;

import static ajedrez.testsupport.Tableros.conjunto;
import static ajedrez.testsupport.Tableros.movimientos;
import static ajedrez.testsupport.Tableros.notaciones;
import static ajedrez.testsupport.Tableros.poner;
import static ajedrez.testsupport.Tableros.vacio;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Cada pieza se prueba sola, sobre un tablero armado a mano: sin partida, sin UI. */
class MovimientosTest {

    // ---------- Peón ----------

    @Test
    void peonBlancoAvanzaUnoODosDesdeSuPosicionInicial() {
        Tablero t = vacio();
        poner(t, "e2", PiezaFactory.peon(Color.BLANCAS));
        assertEquals(conjunto("e3", "e4"), movimientos(t, "e2"));
    }

    @Test
    void peonNegroAvanzaHaciaAbajo() {
        Tablero t = vacio();
        poner(t, "e7", PiezaFactory.peon(Color.NEGRAS));
        assertEquals(conjunto("e6", "e5"), movimientos(t, "e7"));
    }

    @Test
    void peonQueYaSeMovioSoloAvanzaUno() {
        Tablero t = vacio();
        Pieza peon = poner(t, "e2", PiezaFactory.peon(Color.BLANCAS));
        peon.registrarMovimiento();
        assertEquals(conjunto("e3"), movimientos(t, "e2"));
    }

    @Test
    void peonNoAvanzaSiTieneUnaPiezaDelante() {
        Tablero t = vacio();
        poner(t, "e2", PiezaFactory.peon(Color.BLANCAS));
        poner(t, "e3", PiezaFactory.peon(Color.NEGRAS));
        assertEquals(conjunto(), movimientos(t, "e2"));
    }

    @Test
    void peonNoSaltaSobreUnaPiezaParaElDoblePaso() {
        Tablero t = vacio();
        poner(t, "e2", PiezaFactory.peon(Color.BLANCAS));
        poner(t, "e4", PiezaFactory.peon(Color.NEGRAS));
        assertEquals(conjunto("e3"), movimientos(t, "e2"));
    }

    @Test
    void peonCapturaEnDiagonalSoloPiezasRivales() {
        Tablero t = vacio();
        Pieza peon = poner(t, "e4", PiezaFactory.peon(Color.BLANCAS));
        peon.registrarMovimiento(); // ya se movió: solo avanza una casilla
        poner(t, "d5", PiezaFactory.peon(Color.NEGRAS));
        poner(t, "f5", PiezaFactory.peon(Color.BLANCAS));
        assertEquals(conjunto("e5", "d5"), movimientos(t, "e4"));
    }

    @Test
    void peonAmenazaSusDiagonalesAunqueEstenVacias() {
        Tablero t = vacio();
        Pieza peon = poner(t, "e4", PiezaFactory.peon(Color.BLANCAS));
        assertEquals(conjunto("d5", "f5"),
                notaciones(peon.casillasAtacadas(Posicion.de("e4"), t)));
    }

    @Test
    void peonEnElBordeSoloAmenazaUnaDiagonal() {
        Tablero t = vacio();
        Pieza peon = poner(t, "a2", PiezaFactory.peon(Color.BLANCAS));
        assertEquals(conjunto("b3"), notaciones(peon.casillasAtacadas(Posicion.de("a2"), t)));
    }

    // ---------- Torre ----------

    @Test
    void torreEnTableroVacioTiene14Movimientos() {
        Tablero t = vacio();
        poner(t, "d4", PiezaFactory.torre(Color.BLANCAS));
        assertEquals(14, movimientos(t, "d4").size());
    }

    @Test
    void torreSeDetieneAnteUnaPiezaPropiaYCapturaUnaRival() {
        Tablero t = vacio();
        poner(t, "d4", PiezaFactory.torre(Color.BLANCAS));
        poner(t, "d6", PiezaFactory.peon(Color.BLANCAS));
        poner(t, "g4", PiezaFactory.peon(Color.NEGRAS));
        assertEquals(
                conjunto("d5", "d3", "d2", "d1", "c4", "b4", "a4", "e4", "f4", "g4"),
                movimientos(t, "d4"));
    }

    // ---------- Alfil ----------

    @Test
    void alfilEnTableroVacioTiene13Movimientos() {
        Tablero t = vacio();
        poner(t, "d4", PiezaFactory.alfil(Color.BLANCAS));
        assertEquals(13, movimientos(t, "d4").size());
    }

    @Test
    void alfilSoloSeMueveEnDiagonal() {
        Tablero t = vacio();
        poner(t, "a1", PiezaFactory.alfil(Color.BLANCAS));
        assertEquals(conjunto("b2", "c3", "d4", "e5", "f6", "g7", "h8"), movimientos(t, "a1"));
    }

    // ---------- Caballo ----------

    @Test
    void caballoEnLaEsquinaTiene2Movimientos() {
        Tablero t = vacio();
        poner(t, "a1", PiezaFactory.caballo(Color.BLANCAS));
        assertEquals(conjunto("b3", "c2"), movimientos(t, "a1"));
    }

    /** Rodeado de piezas en d4 conserva los mismos 8 saltos que con el tablero vacío. */
    @Test
    void caballoSaltaPorEncimaDeOtrasPiezas() {
        Tablero t = vacio();
        poner(t, "d4", PiezaFactory.caballo(Color.BLANCAS));
        poner(t, "c4", PiezaFactory.peon(Color.BLANCAS));
        poner(t, "e4", PiezaFactory.peon(Color.BLANCAS));
        poner(t, "d3", PiezaFactory.peon(Color.BLANCAS));
        poner(t, "d5", PiezaFactory.peon(Color.BLANCAS));
        assertEquals(8, movimientos(t, "d4").size());
    }

    @Test
    void caballoNoCapturaPiezasPropias() {
        Tablero t = vacio();
        poner(t, "a1", PiezaFactory.caballo(Color.BLANCAS));
        poner(t, "b3", PiezaFactory.peon(Color.BLANCAS));
        assertEquals(conjunto("c2"), movimientos(t, "a1"));
    }

    // ---------- Reina ----------

    @Test
    void reinaEnTableroVacioTiene27Movimientos() {
        Tablero t = vacio();
        poner(t, "d4", PiezaFactory.reina(Color.BLANCAS));
        assertEquals(27, movimientos(t, "d4").size());
    }

    // ---------- Rey ----------

    @Test
    void reyCentralTiene8Movimientos() {
        Tablero t = vacio();
        poner(t, "d4", PiezaFactory.rey(Color.BLANCAS));
        assertEquals(8, movimientos(t, "d4").size());
    }

    @Test
    void reyEnLaEsquinaTiene3Movimientos() {
        Tablero t = vacio();
        poner(t, "a1", PiezaFactory.rey(Color.BLANCAS));
        assertEquals(conjunto("a2", "b1", "b2"), movimientos(t, "a1"));
    }
}
