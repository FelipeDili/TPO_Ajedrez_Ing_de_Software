package ajedrez.core.comando;

import ajedrez.core.fabrica.Promocion;
import ajedrez.core.modelo.Pieza;
import ajedrez.core.modelo.Posicion;
import ajedrez.core.modelo.Tablero;

import java.util.Objects;

/**
 * Mueve una pieza recordando lo necesario para revertirlo: la pieza capturada, el contador
 * de movimientos de la pieza (para que el peón recupere su doble paso al deshacer) y, si
 * hubo promoción, el peón original que la pieza nueva reemplazó.
 */
public class ComandoMoverPieza implements ComandoMovimiento {

    private final Tablero tablero;
    private final Posicion origen;
    private final Posicion destino;
    private final Promocion promocion;
    private Pieza piezaMovida;
    private Pieza piezaCapturada;
    private Pieza piezaPromovida;
    private boolean ejecutado;

    public ComandoMoverPieza(Tablero tablero, Posicion origen, Posicion destino, Promocion promocion) {
        this.tablero = tablero;
        this.origen = origen;
        this.destino = destino;
        this.promocion = Objects.requireNonNull(promocion);
    }

    @Override
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
        ejecutado = true;
    }

    @Override
    public void deshacer() {
        if (!ejecutado) {
            throw new IllegalStateException("El comando todavía no fue ejecutado");
        }
        if (piezaPromovida != null) {
            tablero.colocar(destino, piezaMovida); // vuelve el peón en lugar de la pieza nueva
            piezaPromovida = null;
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
