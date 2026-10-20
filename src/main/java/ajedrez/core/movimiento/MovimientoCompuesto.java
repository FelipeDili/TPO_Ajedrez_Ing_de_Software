package ajedrez.core.movimiento;

import ajedrez.core.modelo.MovimientoEstrategia;
import ajedrez.core.modelo.Posicion;
import ajedrez.core.modelo.Tablero;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Combina varias estrategias en una (unión de sus movimientos). La reina es torre + alfil,
 * y se pueden inventar piezas nuevas (por ejemplo torre + caballo) sin escribir lógica nueva.
 */
public class MovimientoCompuesto implements MovimientoEstrategia {

    private final List<MovimientoEstrategia> partes;

    public MovimientoCompuesto(MovimientoEstrategia... partes) {
        this.partes = List.of(partes);
    }

    @Override
    public List<Posicion> movimientosPosibles(Posicion origen, Tablero tablero) {
        Set<Posicion> union = new LinkedHashSet<>();
        for (MovimientoEstrategia parte : partes) {
            union.addAll(parte.movimientosPosibles(origen, tablero));
        }
        return new ArrayList<>(union);
    }

    @Override
    public List<Posicion> casillasAtacadas(Posicion origen, Tablero tablero) {
        Set<Posicion> union = new LinkedHashSet<>();
        for (MovimientoEstrategia parte : partes) {
            union.addAll(parte.casillasAtacadas(origen, tablero));
        }
        return new ArrayList<>(union);
    }
}
