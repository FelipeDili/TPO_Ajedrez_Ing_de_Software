package ajedrez.testsupport;

import ajedrez.core.fabrica.TableroFactory;
import ajedrez.core.modelo.Pieza;
import ajedrez.core.modelo.Posicion;
import ajedrez.core.modelo.Tablero;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** Utilidades para armar posiciones de prueba con notación algebraica ("e4"). */
public final class Tableros {

    private Tableros() {
    }

    public static Tablero vacio() {
        return new Tablero(8, 8);
    }

    /** Coloca la pieza en la casilla y la devuelve, para poder encadenar. */
    public static Pieza poner(Tablero tablero, String casilla, Pieza pieza) {
        tablero.colocar(Posicion.de(casilla), pieza);
        return pieza;
    }

    public static TableroFactory factory(Tablero tablero) {
        return () -> tablero;
    }

    public static Set<String> notaciones(List<Posicion> posiciones) {
        Set<String> resultado = new TreeSet<>();
        for (Posicion p : posiciones) {
            resultado.add(p.toString());
        }
        return resultado;
    }

    public static Set<String> conjunto(String... casillas) {
        return new TreeSet<>(List.of(casillas));
    }

    public static Set<String> movimientos(Tablero tablero, String origen) {
        Posicion pos = Posicion.de(origen);
        return notaciones(tablero.obtenerPieza(pos).movimientosPosibles(pos, tablero));
    }
}
