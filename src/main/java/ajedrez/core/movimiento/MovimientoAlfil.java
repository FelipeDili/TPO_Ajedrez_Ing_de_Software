package ajedrez.core.movimiento;

import ajedrez.core.modelo.MovimientoEstrategia;
import ajedrez.core.modelo.Posicion;
import ajedrez.core.modelo.Tablero;

import java.util.List;

/** Alfil: desliza en diagonal. */
public class MovimientoAlfil implements MovimientoEstrategia {

    private final MovimientoEstrategia base = new MovimientoDeslizante(Direcciones.DIAGONALES);

    @Override
    public List<Posicion> movimientosPosibles(Posicion origen, Tablero tablero) {
        return base.movimientosPosibles(origen, tablero);
    }
}
