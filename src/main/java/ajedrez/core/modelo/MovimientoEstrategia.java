package ajedrez.core.modelo;

import java.util.List;

/**
 * Strategy: cómo se mueve una pieza. Una pieza nueva solo necesita implementar esta interfaz. Los
 * movimientos son pseudo-legales: no consideran si el propio rey queda en jaque (eso lo resuelve el
 * ValidadorDeJugadas).
 */
@FunctionalInterface
public interface MovimientoEstrategia {

    /** Incluye las capturas. */
    List<Posicion> movimientosPosibles(Posicion origen, Tablero tablero);

    /**
     * Por defecto coinciden con sus movimientos; el peón lo redefine porque avanza distinto de como
     * captura.
     */
    default List<Posicion> casillasAtacadas(Posicion origen, Tablero tablero) {
        return movimientosPosibles(origen, tablero);
    }

    /**
     * Movimiento que nunca se deshace en el tablero (el peón): reinicia la regla de los 50
     * movimientos.
     */
    default boolean esIrreversible() {
        return false;
    }

    /** El peón. */
    default boolean promociona() {
        return false;
    }

    /** La torre, compañera del rey en el enroque. */
    default boolean enroca() {
        return false;
    }
}
