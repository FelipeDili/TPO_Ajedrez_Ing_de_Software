package ajedrez.core.partida;

import ajedrez.core.comando.ComandoMoverPieza;
import ajedrez.core.fabrica.Promocion;
import ajedrez.core.modelo.Color;
import ajedrez.core.modelo.Pieza;
import ajedrez.core.modelo.Posicion;
import ajedrez.core.modelo.Tablero;

import java.util.Optional;

/**
 * Una jugada es legal si respeta el movimiento de la pieza y no deja al propio rey en jaque. Para
 * comprobarlo la ejecuta con un ComandoMoverPieza y la deshace.
 */
public class ValidadorDeJugadas {

    /** Vacío si la jugada es legal. */
    public Optional<String> motivoDeIlegalidad(Tablero tablero, Color turno,
                                               Posicion origen, Posicion destino) {
        Optional<String> motivoOrigen = motivoDeOrigenInvalido(tablero, turno, origen);
        if (motivoOrigen.isPresent()) {
            return motivoOrigen;
        }
        if (!tablero.contiene(destino)) {
            return Optional.of("Posición fuera del tablero");
        }
        Pieza pieza = tablero.obtenerPieza(origen);
        if (!pieza.esMovimientoValido(origen, destino, tablero)) {
            return Optional.of("Movimiento inválido para esa pieza: " + origen + " -> " + destino);
        }
        if (dejaAlReyEnJaque(tablero, origen, destino)) {
            return Optional.of("Ese movimiento deja a tu rey en jaque");
        }
        return Optional.empty();
    }

    /** Revisa solo el origen: fuera del tablero, casilla vacía o pieza rival. */
    public Optional<String> motivoDeOrigenInvalido(Tablero tablero, Color turno, Posicion origen) {
        if (!tablero.contiene(origen)) {
            return Optional.of("Posición fuera del tablero");
        }
        Pieza pieza = tablero.obtenerPieza(origen);
        if (pieza == null) {
            return Optional.of("No hay ninguna pieza en " + origen);
        }
        if (pieza.color() != turno) {
            return Optional.of("No es tu turno: juegan las " + turno.name().toLowerCase());
        }
        return Optional.empty();
    }

    public boolean esLegal(Tablero tablero, Color turno, Posicion origen, Posicion destino) {
        return motivoDeIlegalidad(tablero, turno, origen, destino).isEmpty();
    }

    /** Base para detectar mate y ahogado. */
    public boolean tieneMovimientoLegal(Tablero tablero, Color color) {
        for (Posicion origen : tablero.posicionesDe(color)) {
            Pieza pieza = tablero.obtenerPieza(origen);
            for (Posicion destino : pieza.movimientosPosibles(origen, tablero)) {
                if (!dejaAlReyEnJaque(tablero, origen, destino)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean dejaAlReyEnJaque(Tablero tablero, Posicion origen, Posicion destino) {
        Color color = tablero.obtenerPieza(origen).color();
        // La pieza elegida al promocionar no cambia si el propio rey queda en jaque.
        ComandoMoverPieza prueba = new ComandoMoverPieza(tablero, origen, destino, Promocion.DAMA);
        prueba.ejecutar();
        boolean enJaque = tablero.evaluarJaque(color);
        prueba.deshacer();
        return enJaque;
    }
}
