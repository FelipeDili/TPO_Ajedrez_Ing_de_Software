package ajedrez.core.movimiento;

import ajedrez.core.modelo.MovimientoEstrategia;
import ajedrez.core.modelo.Posicion;
import ajedrez.core.modelo.Tablero;

import java.util.List;

/** Torre: desliza en línea recta (filas y columnas). */
public class MovimientoTorre implements MovimientoEstrategia {

    private final MovimientoEstrategia base = new MovimientoDeslizante(Direcciones.ORTOGONALES);

    @Override
    public List<Posicion> movimientosPosibles(Posicion origen, Tablero tablero) {
        return base.movimientosPosibles(origen, tablero);
    }
}
