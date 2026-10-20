package ajedrez.core.modelo;

import ajedrez.core.fabrica.PiezaFactory;
import org.junit.jupiter.api.Test;

import static ajedrez.testsupport.Tableros.poner;
import static ajedrez.testsupport.Tableros.vacio;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TableroTest {

    @Test
    void elTamanioEsUnParametro() {
        Tablero tablero = new Tablero(10, 6);
        assertEquals(10, tablero.filas());
        assertEquals(6, tablero.columnas());
        assertTrue(tablero.contiene(Posicion.de("f10")));
        assertFalse(tablero.contiene(Posicion.de("g1")));
        assertFalse(tablero.contiene(Posicion.de("a11")));
    }

    @Test
    void noPermiteTablerosSinCasillas() {
        assertThrows(IllegalArgumentException.class, () -> new Tablero(0, 8));
    }

    @Test
    void colocarFueraDelTableroFalla() {
        Tablero tablero = vacio();
        assertThrows(IllegalArgumentException.class,
                () -> tablero.colocar(new Posicion(8, 0), PiezaFactory.peon(Color.BLANCAS)));
    }

    @Test
    void moverDejaVaciaLaCasillaDeOrigenYDevuelveLaCapturada() {
        Tablero tablero = vacio();
        Pieza torre = poner(tablero, "a1", PiezaFactory.torre(Color.BLANCAS));
        Pieza peon = poner(tablero, "a5", PiezaFactory.peon(Color.NEGRAS));

        Pieza capturada = tablero.mover(Posicion.de("a1"), Posicion.de("a5"));

        assertSame(peon, capturada);
        assertSame(torre, tablero.obtenerPieza(Posicion.de("a5")));
        assertNull(tablero.obtenerPieza(Posicion.de("a1")));
    }

    @Test
    void moverDesdeUnaCasillaVaciaFalla() {
        assertThrows(IllegalStateException.class,
                () -> vacio().mover(Posicion.de("a1"), Posicion.de("a2")));
    }

    @Test
    void detectaJaquePorTorre() {
        Tablero tablero = vacio();
        poner(tablero, "e1", PiezaFactory.rey(Color.BLANCAS));
        poner(tablero, "e8", PiezaFactory.torre(Color.NEGRAS));

        assertTrue(tablero.evaluarJaque(Color.BLANCAS));
    }

    @Test
    void unaPiezaInterpuestaBloqueaElJaque() {
        Tablero tablero = vacio();
        poner(tablero, "e1", PiezaFactory.rey(Color.BLANCAS));
        poner(tablero, "e4", PiezaFactory.peon(Color.BLANCAS));
        poner(tablero, "e8", PiezaFactory.torre(Color.NEGRAS));

        assertFalse(tablero.evaluarJaque(Color.BLANCAS));
    }

    @Test
    void detectaJaquePorCaballo() {
        Tablero tablero = vacio();
        poner(tablero, "e1", PiezaFactory.rey(Color.BLANCAS));
        poner(tablero, "f3", PiezaFactory.caballo(Color.NEGRAS));

        assertTrue(tablero.evaluarJaque(Color.BLANCAS));
    }

    @Test
    void elPeonDaJaqueEnDiagonal() {
        Tablero tablero = vacio();
        poner(tablero, "e1", PiezaFactory.rey(Color.BLANCAS));
        poner(tablero, "d2", PiezaFactory.peon(Color.NEGRAS));

        assertTrue(tablero.evaluarJaque(Color.BLANCAS));
    }

    @Test
    void elPeonNoDaJaqueDeFrente() {
        Tablero tablero = vacio();
        poner(tablero, "e1", PiezaFactory.rey(Color.BLANCAS));
        poner(tablero, "e2", PiezaFactory.peon(Color.NEGRAS));

        assertFalse(tablero.evaluarJaque(Color.BLANCAS));
    }

    @Test
    void sinReyNoHayJaque() {
        Tablero tablero = vacio();
        poner(tablero, "e8", PiezaFactory.torre(Color.NEGRAS));
        assertFalse(tablero.evaluarJaque(Color.BLANCAS));
    }

    @Test
    void buscaElReyDeCadaColor() {
        Tablero tablero = vacio();
        poner(tablero, "e1", PiezaFactory.rey(Color.BLANCAS));
        poner(tablero, "e8", PiezaFactory.rey(Color.NEGRAS));

        assertEquals(Posicion.de("e1"), tablero.buscarRey(Color.BLANCAS).orElseThrow());
        assertEquals(Posicion.de("e8"), tablero.buscarRey(Color.NEGRAS).orElseThrow());
    }

    @Test
    void sinReyNoEncuentraNada() {
        assertTrue(vacio().buscarRey(Color.BLANCAS).isEmpty());
    }

    @Test
    void dosTablerosConLasMismasPiezasTienenLaMismaClave() {
        Tablero a = vacio();
        Tablero b = vacio();
        poner(a, "e4", PiezaFactory.peon(Color.BLANCAS));
        poner(b, "e4", PiezaFactory.peon(Color.BLANCAS));

        assertEquals(a.clave(), b.clave());
    }

    @Test
    void dosTablerosConPiezasDistintasTienenClavesDistintas() {
        Tablero a = vacio();
        Tablero b = vacio();
        poner(a, "e4", PiezaFactory.peon(Color.BLANCAS));
        poner(b, "e4", PiezaFactory.peon(Color.NEGRAS));

        assertNotEquals(a.clave(), b.clave());
    }
}
