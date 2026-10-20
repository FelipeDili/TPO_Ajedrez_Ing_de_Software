package ajedrez.core.movimiento;

import ajedrez.core.modelo.MovimientoEstrategia;
import ajedrez.core.modelo.Posicion;
import ajedrez.core.modelo.Tablero;

import java.util.List;

/** Caballo: salta en "L" y puede pasar por encima de otras piezas. */
public class MovimientoCaballo implements MovimientoEstrategia {

    private final MovimientoEstrategia base = new MovimientoSaltador(Direcciones.SALTOS_CABALLO);

    @Override
    public List<Posicion> movimientosPosibles(Posicion origen, Tablero tablero) {
        return base.movimientosPosibles(origen, tablero);
    }
}
