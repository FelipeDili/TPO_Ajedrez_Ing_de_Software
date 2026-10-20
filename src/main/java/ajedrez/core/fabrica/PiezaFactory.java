package ajedrez.core.fabrica;

import ajedrez.core.modelo.Color;
import ajedrez.core.modelo.Pieza;
import ajedrez.core.movimiento.MovimientoAlfil;
import ajedrez.core.movimiento.MovimientoCaballo;
import ajedrez.core.movimiento.MovimientoPeon;
import ajedrez.core.movimiento.MovimientoReina;
import ajedrez.core.movimiento.MovimientoRey;
import ajedrez.core.movimiento.MovimientoTorre;

/** Ensambla las piezas estándar: color + símbolo + estrategia de movimiento. */
public final class PiezaFactory {

    private PiezaFactory() {
    }

    public static Pieza peon(Color color) {
        return new Pieza(color, 'P', false, new MovimientoPeon());
    }

    public static Pieza torre(Color color) {
        return new Pieza(color, 'T', false, new MovimientoTorre());
    }

    public static Pieza caballo(Color color) {
        return new Pieza(color, 'C', false, new MovimientoCaballo());
    }

    public static Pieza alfil(Color color) {
        return new Pieza(color, 'A', false, new MovimientoAlfil());
    }

    public static Pieza reina(Color color) {
        return new Pieza(color, 'D', false, new MovimientoReina());
    }

    public static Pieza rey(Color color) {
        return new Pieza(color, 'R', true, new MovimientoRey());
    }
}
