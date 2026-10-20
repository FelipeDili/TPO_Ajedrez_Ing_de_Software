package ajedrez.core.movimiento;

import ajedrez.core.modelo.MovimientoEstrategia;
import ajedrez.core.modelo.Posicion;
import ajedrez.core.modelo.Tablero;

import java.util.List;

/** Reina: se mueve como una torre y como un alfil (composición de ambas estrategias). */
public class MovimientoReina implements MovimientoEstrategia {

    private final MovimientoEstrategia base =
            new MovimientoCompuesto(new MovimientoTorre(), new MovimientoAlfil());

    @Override
    public List<Posicion> movimientosPosibles(Posicion origen, Tablero tablero) {
        return base.movimientosPosibles(origen, tablero);
    }
}
