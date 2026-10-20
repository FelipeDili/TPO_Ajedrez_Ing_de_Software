package ajedrez.core.movimiento;

import ajedrez.core.modelo.MovimientoEstrategia;
import ajedrez.core.modelo.Pieza;
import ajedrez.core.modelo.Posicion;
import ajedrez.core.modelo.Tablero;

import java.util.ArrayList;
import java.util.List;

/**
 * Bloque reutilizable: un único salto por cada desplazamiento, sin importar lo que haya
 * en el medio. Sirve para el caballo y para el rey.
 */
public class MovimientoSaltador implements MovimientoEstrategia {

    private final int[][] saltos;

    public MovimientoSaltador(int[][] saltos) {
        this.saltos = saltos;
    }

    @Override
    public List<Posicion> movimientosPosibles(Posicion origen, Tablero tablero) {
        Pieza propia = tablero.obtenerPieza(origen);
        List<Posicion> resultado = new ArrayList<>();
        for (int[] s : saltos) {
            Posicion destino = origen.desplazar(s[0], s[1]);
            if (!tablero.contiene(destino)) {
                continue;
            }
            Pieza otra = tablero.obtenerPieza(destino);
            if (otra == null || otra.color() != propia.color()) {
                resultado.add(destino);
            }
        }
        return resultado;
    }
}
