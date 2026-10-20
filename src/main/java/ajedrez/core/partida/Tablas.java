package ajedrez.core.partida;

import ajedrez.core.fabrica.Promocion;
import ajedrez.core.modelo.Posicion;

/** Fase final: la partida terminó empatada por algún {@link MotivoTablas}. */
public class Tablas implements EstadoPartida {

    private final MotivoTablas motivo;

    public Tablas(MotivoTablas motivo) {
        this.motivo = motivo;
    }

    public MotivoTablas motivo() {
        return motivo;
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
        return "tablas por " + motivo.descripcion();
    }
}
