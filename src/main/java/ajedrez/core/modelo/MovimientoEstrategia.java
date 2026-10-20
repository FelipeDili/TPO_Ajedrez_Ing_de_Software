package ajedrez.core.modelo;

import java.util.List;

/**
 * Strategy: define CÓMO se mueve una pieza. Para sumar una pieza nueva alcanza con
 * implementar esta interfaz; ninguna clase existente se modifica.
 *
 * Los movimientos son "pseudo-legales": respetan la forma de moverse de la pieza,
 * pero no consideran si el propio rey queda en jaque (eso lo resuelve el ValidadorDeJugadas).
 */
@FunctionalInterface
public interface MovimientoEstrategia {

    /** Casillas a las que la pieza ubicada en {@code origen} puede ir (incluye capturas). */
    List<Posicion> movimientosPosibles(Posicion origen, Tablero tablero);

    /**
     * Casillas que la pieza amenaza. Por defecto coincide con sus movimientos; el peón
     * lo redefine porque avanza distinto de como captura.
     */
    default List<Posicion> casillasAtacadas(Posicion origen, Tablero tablero) {
        return movimientosPosibles(origen, tablero);
    }

    /**
     * Indica si mover esta pieza es irreversible (el peón nunca retrocede). Se usa para la
     * regla de los 50 movimientos.
     */
    default boolean esIrreversible() {
        return false;
    }

    /** Indica si la pieza se convierte en otra al llegar a la última fila (el peón). */
    default boolean promociona() {
        return false;
    }

    /** Indica si la pieza puede ser la compañera del rey en un enroque (la torre). */
    default boolean enroca() {
        return false;
    }
}
