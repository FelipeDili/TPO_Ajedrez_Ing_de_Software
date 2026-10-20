package ajedrez.core.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Grilla de casilleros. El tamaño es un parámetro (no está fijo en 8x8), así que cambiar
 * las dimensiones no obliga a tocar esta clase.
 */
public class Tablero {

    private final Pieza[][] casilleros;

    public Tablero(int filas, int columnas) {
        if (filas <= 0 || columnas <= 0) {
            throw new IllegalArgumentException("El tablero necesita al menos 1 fila y 1 columna");
        }
        this.casilleros = new Pieza[filas][columnas];
    }

    public int filas() {
        return casilleros.length;
    }

    public int columnas() {
        return casilleros[0].length;
    }

    public boolean contiene(Posicion pos) {
        return pos.fila() >= 0 && pos.fila() < filas()
                && pos.columna() >= 0 && pos.columna() < columnas();
    }

    /** Devuelve la pieza en la posición, o null si está vacía o fuera del tablero. */
    public Pieza obtenerPieza(Posicion pos) {
        return contiene(pos) ? casilleros[pos.fila()][pos.columna()] : null;
    }

    public boolean estaVacia(Posicion pos) {
        return contiene(pos) && obtenerPieza(pos) == null;
    }

    public void colocar(Posicion pos, Pieza pieza) {
        exigirDentro(pos);
        casilleros[pos.fila()][pos.columna()] = pieza;
    }

    /** Mueve la pieza de origen a destino y devuelve la pieza capturada (o null). */
    public Pieza mover(Posicion origen, Posicion destino) {
        Pieza movida = obtenerPieza(origen);
        if (movida == null) {
            throw new IllegalStateException("No hay pieza en " + origen);
        }
        exigirDentro(destino);
        Pieza capturada = obtenerPieza(destino);
        colocar(origen, null);
        colocar(destino, movida);
        return capturada;
    }

    public List<Posicion> posicionesDe(Color color) {
        List<Posicion> resultado = new ArrayList<>();
        for (int f = 0; f < filas(); f++) {
            for (int c = 0; c < columnas(); c++) {
                Pieza p = casilleros[f][c];
                if (p != null && p.color() == color) {
                    resultado.add(new Posicion(f, c));
                }
            }
        }
        return resultado;
    }

    public Optional<Posicion> buscarRey(Color color) {
        for (Posicion pos : posicionesDe(color)) {
            if (obtenerPieza(pos).esRey()) {
                return Optional.of(pos);
            }
        }
        return Optional.empty();
    }

    /** true si el rey de ese color está amenazado por alguna pieza rival. */
    public boolean evaluarJaque(Color color) {
        Optional<Posicion> rey = buscarRey(color);
        if (rey.isEmpty()) {
            return false;
        }
        for (Posicion pos : posicionesDe(color.opuesto())) {
            if (obtenerPieza(pos).casillasAtacadas(pos, this).contains(rey.get())) {
                return true;
            }
        }
        return false;
    }

    /** Foto textual de la posición, útil para detectar repeticiones. */
    public String clave() {
        StringBuilder sb = new StringBuilder();
        for (int f = 0; f < filas(); f++) {
            for (int c = 0; c < columnas(); c++) {
                Pieza p = casilleros[f][c];
                sb.append(p == null ? '.' : p.representacion());
            }
            sb.append('/');
        }
        return sb.toString();
    }

    private void exigirDentro(Posicion pos) {
        if (!contiene(pos)) {
            throw new IllegalArgumentException("Posición fuera del tablero: " + pos);
        }
    }
}
