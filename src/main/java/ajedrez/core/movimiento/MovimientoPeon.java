package ajedrez.core.movimiento;

import ajedrez.core.modelo.Color;
import ajedrez.core.modelo.MovimientoEstrategia;
import ajedrez.core.modelo.Pieza;
import ajedrez.core.modelo.Posicion;
import ajedrez.core.modelo.Tablero;

import java.util.ArrayList;
import java.util.List;

/**
 * Peón: avanza una casilla (dos si todavía no se movió) sin capturar, y captura en
 * diagonal hacia adelante. La dirección depende del color. Al llegar a la última fila
 * promociona (ver {@link #promociona()}). La captura al paso no está implementada (es opcional).
 */
public class MovimientoPeon implements MovimientoEstrategia {

    @Override
    public List<Posicion> movimientosPosibles(Posicion origen, Tablero tablero) {
        Pieza peon = tablero.obtenerPieza(origen);
        int dir = direccion(peon);
        List<Posicion> resultado = new ArrayList<>();

        Posicion uno = origen.desplazar(dir, 0);
        if (tablero.estaVacia(uno)) {
            resultado.add(uno);
            Posicion dos = uno.desplazar(dir, 0);
            if (!peon.seHaMovido() && tablero.estaVacia(dos)) {
                resultado.add(dos);
            }
        }

        for (Posicion diagonal : casillasAtacadas(origen, tablero)) {
            Pieza otra = tablero.obtenerPieza(diagonal);
            if (otra != null && otra.color() != peon.color()) {
                resultado.add(diagonal);
            }
        }
        return resultado;
    }

    /** El peón amenaza sus dos diagonales delanteras, haya o no una pieza ahí. */
    @Override
    public List<Posicion> casillasAtacadas(Posicion origen, Tablero tablero) {
        int dir = direccion(tablero.obtenerPieza(origen));
        List<Posicion> resultado = new ArrayList<>();
        for (int dc : new int[]{-1, 1}) {
            Posicion diagonal = origen.desplazar(dir, dc);
            if (tablero.contiene(diagonal)) {
                resultado.add(diagonal);
            }
        }
        return resultado;
    }

    @Override
    public boolean esIrreversible() {
        return true;
    }

    @Override
    public boolean promociona() {
        return true;
    }

    private int direccion(Pieza peon) {
        return peon.color() == Color.BLANCAS ? 1 : -1;
    }
}
