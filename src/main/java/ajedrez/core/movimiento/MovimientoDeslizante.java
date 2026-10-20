package ajedrez.core.movimiento;

import ajedrez.core.modelo.MovimientoEstrategia;
import ajedrez.core.modelo.Pieza;
import ajedrez.core.modelo.Posicion;
import ajedrez.core.modelo.Tablero;

import java.util.ArrayList;
import java.util.List;

/**
 * Bloque reutilizable: avanza en línea recta por cada dirección hasta chocar con el borde,
 * una pieza propia (se detiene antes) o una enemiga (puede capturarla y se detiene).
 */
public class MovimientoDeslizante implements MovimientoEstrategia {

    private final int[][] direcciones;

    public MovimientoDeslizante(int[][] direcciones) {
        this.direcciones = direcciones;
    }

    @Override
    public List<Posicion> movimientosPosibles(Posicion origen, Tablero tablero) {
        Pieza propia = tablero.obtenerPieza(origen);
        List<Posicion> resultado = new ArrayList<>();
        for (int[] d : direcciones) {
            Posicion actual = origen.desplazar(d[0], d[1]);
            while (tablero.contiene(actual)) {
                Pieza otra = tablero.obtenerPieza(actual);
                if (otra == null) {
                    resultado.add(actual);
                } else {
                    if (otra.color() != propia.color()) {
                        resultado.add(actual);
                    }
                    break;
                }
                actual = actual.desplazar(d[0], d[1]);
            }
        }
        return resultado;
    }
}
