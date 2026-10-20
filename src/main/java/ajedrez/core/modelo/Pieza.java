package ajedrez.core.modelo;

import java.util.List;
import java.util.Objects;

/**
 * Pieza genérica: tiene color y DELEGA su forma de moverse en una estrategia (composición
 * sobre herencia). No existe una subclase por tipo de pieza.
 */
public class Pieza {

    private final Color color;
    private final char simbolo;
    private final boolean esRey;
    private final MovimientoEstrategia estrategia;
    private int cantidadMovimientos;

    /**
     * @param simbolo letra en mayúscula con la que se dibuja (se muestra en minúscula si es negra)
     * @param esRey   true si capturarla (jaque mate) define el final de la partida
     */
    public Pieza(Color color, char simbolo, boolean esRey, MovimientoEstrategia estrategia) {
        this.color = Objects.requireNonNull(color);
        this.simbolo = Character.toUpperCase(simbolo);
        this.esRey = esRey;
        this.estrategia = Objects.requireNonNull(estrategia);
    }

    public Color color() {
        return color;
    }

    public boolean esRey() {
        return esRey;
    }

    /** Carácter para dibujar la pieza: mayúscula si es blanca, minúscula si es negra. */
    public char representacion() {
        return color == Color.BLANCAS ? simbolo : Character.toLowerCase(simbolo);
    }

    public List<Posicion> movimientosPosibles(Posicion origen, Tablero tablero) {
        return estrategia.movimientosPosibles(origen, tablero);
    }

    public List<Posicion> casillasAtacadas(Posicion origen, Tablero tablero) {
        return estrategia.casillasAtacadas(origen, tablero);
    }

    public boolean esMovimientoValido(Posicion origen, Posicion destino, Tablero tablero) {
        return estrategia.movimientosPosibles(origen, tablero).contains(destino);
    }

    public boolean esMovimientoIrreversible() {
        return estrategia.esIrreversible();
    }

    /**
     * true si al llegar a {@code destino} la pieza se promociona: su estrategia lo permite y
     * es la última fila del lado rival. Se calcula con el tamaño del tablero, no con un 8 fijo.
     */
    public boolean promocionaEn(Posicion destino, Tablero tablero) {
        int ultimaFila = color == Color.BLANCAS ? tablero.filas() - 1 : 0;
        return estrategia.promociona() && destino.fila() == ultimaFila;
    }

    /** true si todavía puede enrocar con el rey: es una torre que nunca se movió. */
    public boolean puedeEnrocar() {
        return estrategia.enroca() && !seHaMovido();
    }

    public boolean seHaMovido() {
        return cantidadMovimientos > 0;
    }

    public void registrarMovimiento() {
        cantidadMovimientos++;
    }

    public void revertirMovimiento() {
        if (cantidadMovimientos == 0) {
            throw new IllegalStateException("La pieza no tiene movimientos para revertir");
        }
        cantidadMovimientos--;
    }

    @Override
    public String toString() {
        return String.valueOf(representacion());
    }
}
