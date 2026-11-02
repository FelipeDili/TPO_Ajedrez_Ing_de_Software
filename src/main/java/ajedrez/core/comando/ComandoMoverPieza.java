package ajedrez.core.comando;

import ajedrez.core.fabrica.Promocion;
import ajedrez.core.modelo.Pieza;
import ajedrez.core.modelo.Posicion;
import ajedrez.core.modelo.Tablero;

import java.util.Objects;

/**
 * Command: una jugada que se puede ejecutar y deshacer. Guarda lo necesario para revertirla: la
 * pieza capturada, el contador de movimientos, el peón que reemplazó una promoción y de dónde salió
 * la torre en un enroque. No tiene interfaz porque hay un solo tipo de comando.
 */
public class ComandoMoverPieza {

    private final Tablero tablero;
    private final Posicion origen;
    private final Posicion destino;
    private final Promocion promocion;
    private Pieza piezaMovida;
    private Pieza piezaCapturada;
    private Pieza piezaPromovida;
    private Posicion torreOrigen;
    private Posicion torreDestino;
    private boolean ejecutado;

    public ComandoMoverPieza(Tablero tablero, Posicion origen, Posicion destino, Promocion promocion) {
        this.tablero = tablero;
        this.origen = origen;
        this.destino = destino;
        this.promocion = Objects.requireNonNull(promocion);
    }

    public void ejecutar() {
        if (ejecutado) {
            throw new IllegalStateException("El comando ya fue ejecutado");
        }
        piezaMovida = tablero.obtenerPieza(origen);
        boolean promociona = piezaMovida.promocionaEn(destino, tablero);
        piezaCapturada = tablero.mover(origen, destino);
        piezaMovida.registrarMovimiento();
        if (promociona) {
            piezaPromovida = promocion.crear(piezaMovida.color());
            tablero.colocar(destino, piezaPromovida);
        }
        if (esEnroque()) {
            moverTorreDelEnroque();
        }
        ejecutado = true;
    }

    /** El rey nunca avanza dos columnas salvo al enrocar. */
    private boolean esEnroque() {
        return piezaMovida.esRey() && Math.abs(destino.columna() - origen.columna()) == 2;
    }

    /** La torre es la primera pieza más allá del rey en esa dirección; salta a su lado. */
    private void moverTorreDelEnroque() {
        int direccion = Integer.signum(destino.columna() - origen.columna());
        torreOrigen = destino.desplazar(0, direccion);
        while (tablero.obtenerPieza(torreOrigen) == null) {
            torreOrigen = torreOrigen.desplazar(0, direccion);
        }
        torreDestino = origen.desplazar(0, direccion);
        tablero.mover(torreOrigen, torreDestino);
        tablero.obtenerPieza(torreDestino).registrarMovimiento();
    }

    public void deshacer() {
        if (!ejecutado) {
            throw new IllegalStateException("El comando todavía no fue ejecutado");
        }
        if (piezaPromovida != null) {
            tablero.colocar(destino, piezaMovida); // vuelve el peón en lugar de la pieza nueva
            piezaPromovida = null;
        }
        if (torreOrigen != null) {
            tablero.obtenerPieza(torreDestino).revertirMovimiento();
            tablero.mover(torreDestino, torreOrigen);
            torreOrigen = null;
            torreDestino = null;
        }
        tablero.mover(destino, origen);
        if (piezaCapturada != null) {
            tablero.colocar(destino, piezaCapturada);
        }
        piezaMovida.revertirMovimiento();
        ejecutado = false;
    }

    public Pieza piezaMovida() {
        return piezaMovida;
    }

    public Pieza piezaCapturada() {
        return piezaCapturada;
    }
}
