package ajedrez.core.modelo;

public enum Color {
    BLANCAS,
    NEGRAS;

    public Color opuesto() {
        return this == BLANCAS ? NEGRAS : BLANCAS;
    }
}
