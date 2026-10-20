package ajedrez.core.movimiento;

/** Vectores {deltaFila, deltaColumna} reutilizados por varias estrategias. */
final class Direcciones {

    static final int[][] ORTOGONALES = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    static final int[][] DIAGONALES = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
    static final int[][] TODAS = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };
    static final int[][] SALTOS_CABALLO = {
            {2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
    };

    private Direcciones() {
    }
}
