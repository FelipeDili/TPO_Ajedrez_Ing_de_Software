package ajedrez.core.partida;

import ajedrez.core.fabrica.Promocion;
import ajedrez.core.modelo.Color;
import ajedrez.core.modelo.Posicion;

/** Fase final: un rey está en jaque y no tiene ninguna jugada legal. */
public class JaqueMate implements EstadoPartida {

    private final Color ganador;

    public JaqueMate(Color ganador) {
        this.ganador = ganador;
    }

    public Color ganador() {
        return ganador;
    }

    @Override
    public void intentarMovimiento(Partida partida, Posicion origen, Posicion destino,
                                   Promocion promocion) {
        partida.notificador().alError("La partida terminó: " + descripcion());
    }

    @Override
    public boolean estaTerminada() {
        return true;
    }

    @Override
    public String descripcion() {
        return "jaque mate, ganan las " + ganador.name().toLowerCase();
    }
}
