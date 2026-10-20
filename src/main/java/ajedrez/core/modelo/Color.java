package ajedrez.core.modelo;

/** Bando de una pieza o jugador. */
public enum Color {
    BLANCAS,
    NEGRAS;

    public Color opuesto() {
        return this == BLANCAS ? NEGRAS : BLANCAS;
    }
}
