package ajedrez.core.movimiento;

import ajedrez.core.modelo.MovimientoEstrategia;
import ajedrez.core.modelo.Posicion;
import ajedrez.core.modelo.Tablero;

import java.util.List;

/** Rey: una casilla en cualquier dirección. (Enroque no implementado: es opcional.) */
public class MovimientoRey implements MovimientoEstrategia {

    private final MovimientoEstrategia base = new MovimientoSaltador(Direcciones.TODAS);

    @Override
    public List<Posicion> movimientosPosibles(Posicion origen, Tablero tablero) {
        return base.movimientosPosibles(origen, tablero);
    }
}
