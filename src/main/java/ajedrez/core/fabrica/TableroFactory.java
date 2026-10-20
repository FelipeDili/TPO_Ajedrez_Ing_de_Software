package ajedrez.core.fabrica;

import ajedrez.core.modelo.Tablero;

/**
 * Crea el tablero inicial de una partida. Al inyectar otra implementación se puede jugar
 * con otro tamaño, otras piezas o una posición armada para un test.
 */
@FunctionalInterface
public interface TableroFactory {

    Tablero crearTableroInicial();
}
