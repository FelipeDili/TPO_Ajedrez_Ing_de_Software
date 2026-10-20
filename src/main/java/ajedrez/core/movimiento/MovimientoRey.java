package ajedrez.core.movimiento;

import ajedrez.core.modelo.MovimientoEstrategia;
import ajedrez.core.modelo.Pieza;
import ajedrez.core.modelo.Posicion;
import ajedrez.core.modelo.Tablero;

import java.util.ArrayList;
import java.util.List;

/**
 * Rey: una casilla en cualquier dirección, o el enroque: dos casillas hacia una torre propia
 * cuando ninguno de los dos se movió, no hay piezas entre ellos, el rey no está en jaque y
 * no pasa por una casilla atacada. Que no termine en jaque lo revisa el ValidadorDeJugadas,
 * como en cualquier jugada. El salto de la torre lo hace el ComandoMoverPieza.
 */
public class MovimientoRey implements MovimientoEstrategia {

    private final MovimientoEstrategia base = new MovimientoSaltador(Direcciones.TODAS);

    @Override
    public List<Posicion> movimientosPosibles(Posicion origen, Tablero tablero) {
        List<Posicion> resultado = new ArrayList<>(base.movimientosPosibles(origen, tablero));
        for (int direccion : new int[]{-1, 1}) {
            if (puedeEnrocarHacia(origen, direccion, tablero)) {
                resultado.add(origen.desplazar(0, 2 * direccion));
            }
        }
        return resultado;
    }

    /** El rey amenaza solo las casillas de alrededor: el enroque nunca es una captura. */
    @Override
    public List<Posicion> casillasAtacadas(Posicion origen, Tablero tablero) {
        return base.movimientosPosibles(origen, tablero);
    }

    private boolean puedeEnrocarHacia(Posicion origen, int direccion, Tablero tablero) {
        Pieza rey = tablero.obtenerPieza(origen);
        if (rey.seHaMovido()) {
            return false;
        }
        Posicion torre = primeraPiezaHacia(origen, direccion, tablero);
        if (torre == null || Math.abs(torre.columna() - origen.columna()) < 3) {
            return false; // sin espacio para que el rey avance dos y la torre lo salte
        }
        Pieza companera = tablero.obtenerPieza(torre);
        Posicion casillaDePaso = origen.desplazar(0, direccion);
        return companera.color() == rey.color()
                && companera.puedeEnrocar()
                && !tablero.evaluarJaque(rey.color())
                && !tablero.estaAtacada(casillaDePaso, rey.color().opuesto());
    }

    /** Recorre la fila desde el rey hacia un lado y devuelve la primera casilla ocupada. */
    private Posicion primeraPiezaHacia(Posicion origen, int direccion, Tablero tablero) {
        Posicion actual = origen.desplazar(0, direccion);
        while (tablero.contiene(actual)) {
            if (tablero.obtenerPieza(actual) != null) {
                return actual;
            }
            actual = actual.desplazar(0, direccion);
        }
        return null;
    }
}
