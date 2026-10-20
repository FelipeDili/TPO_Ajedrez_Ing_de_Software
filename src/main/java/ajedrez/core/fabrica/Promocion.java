package ajedrez.core.fabrica;

import ajedrez.core.modelo.Color;
import ajedrez.core.modelo.Pieza;

import java.util.function.Function;

/** Piezas en las que se puede convertir un peón al llegar a la última fila. */
public enum Promocion {
    DAMA(PiezaFactory::reina),
    TORRE(PiezaFactory::torre),
    ALFIL(PiezaFactory::alfil),
    CABALLO(PiezaFactory::caballo);

    private final Function<Color, Pieza> fabrica;

    Promocion(Function<Color, Pieza> fabrica) {
        this.fabrica = fabrica;
    }

    public Pieza crear(Color color) {
        return fabrica.apply(color);
    }
}
