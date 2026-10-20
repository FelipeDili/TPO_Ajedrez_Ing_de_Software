package ajedrez.core.partida;

import ajedrez.core.fabrica.Promocion;
import ajedrez.core.modelo.Color;
import ajedrez.core.modelo.Posicion;
import ajedrez.core.modelo.Tablero;

import java.util.Optional;

/** Fase normal: valida la jugada, la aplica y evalúa si la partida termina. */
public class EnCurso implements EstadoPartida {

    @Override
    public void intentarMovimiento(Partida partida, Posicion origen, Posicion destino,
                                   Promocion promocion) {
        Tablero tablero = partida.tablero();
        Optional<String> motivo = partida.validador()
                .motivoDeIlegalidad(tablero, partida.turnoActual(), origen, destino);
        if (motivo.isPresent()) {
            partida.notificador().alError(motivo.get());
            return;
        }

        partida.aplicarJugada(origen, destino, promocion);
        evaluarSituacion(partida);
    }

    private void evaluarSituacion(Partida partida) {
        Tablero tablero = partida.tablero();
        Color quienMueve = partida.turnoActual();
        boolean enJaque = tablero.evaluarJaque(quienMueve);
        boolean sinJugadas = !partida.validador().tieneMovimientoLegal(tablero, quienMueve);

        if (sinJugadas && enJaque) {
            partida.cambiarEstado(new JaqueMate(quienMueve.opuesto()));
        } else if (sinJugadas) {
            partida.cambiarEstado(new Tablas(MotivoTablas.AHOGADO));
        } else if (partida.medioMovimientosSinProgreso() >= partida.limiteMedioMovimientos()) {
            partida.cambiarEstado(new Tablas(MotivoTablas.REGLA_DE_LOS_50_MOVIMIENTOS));
        } else if (partida.repeticionesDePosicionActual() >= 3) {
            partida.cambiarEstado(new Tablas(MotivoTablas.REPETICION));
        }

        partida.notificador().alTableroActualizado(tablero);
        if (enJaque) {
            partida.notificador().alJaque(quienMueve);
        }
        if (partida.estado().estaTerminada()) {
            partida.notificador().alFinDePartida(partida.estado().descripcion());
        }
    }

    @Override
    public boolean estaTerminada() {
        return false;
    }

    @Override
    public String descripcion() {
        return "en curso";
    }
}
