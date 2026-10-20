package ajedrez.core.fabrica;

import ajedrez.core.modelo.Color;
import ajedrez.core.modelo.Pieza;
import ajedrez.core.modelo.Posicion;
import ajedrez.core.modelo.Tablero;

import java.util.List;
import java.util.function.Function;

/** Tablero de ajedrez clásico: 8x8 con las 32 piezas en su posición inicial. */
public class TableroEstandarFactory implements TableroFactory {

    private static final List<Function<Color, Pieza>> FILA_TRASERA = List.of(
            PiezaFactory::torre,
            PiezaFactory::caballo,
            PiezaFactory::alfil,
            PiezaFactory::reina,
            PiezaFactory::rey,
            PiezaFactory::alfil,
            PiezaFactory::caballo,
            PiezaFactory::torre
    );

    @Override
    public Tablero crearTableroInicial() {
        Tablero tablero = new Tablero(8, 8);
        armarBando(tablero, Color.BLANCAS, 0, 1);
        armarBando(tablero, Color.NEGRAS, 7, 6);
        return tablero;
    }

    private void armarBando(Tablero tablero, Color color, int filaTrasera, int filaPeones) {
        for (int columna = 0; columna < FILA_TRASERA.size(); columna++) {
            tablero.colocar(new Posicion(filaTrasera, columna), FILA_TRASERA.get(columna).apply(color));
            tablero.colocar(new Posicion(filaPeones, columna), PiezaFactory.peon(color));
        }
    }
}
