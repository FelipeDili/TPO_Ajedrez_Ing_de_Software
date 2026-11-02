package ajedrez.core.partida;

import ajedrez.core.fabrica.Promocion;
import ajedrez.core.modelo.Color;
import ajedrez.core.modelo.Posicion;

import java.util.Optional;

/**
 * Puerto de entrada: acciones y consultas que cualquier interfaz (consola, gráfica, web) le pide al
 * núcleo.
 */
public interface EntradaAjedrez {

    void iniciar();

    /** Si la jugada lleva un peón a la última fila, se convierte en la pieza indicada. */
    void intentarMovimiento(Posicion origen, Posicion destino, Promocion promocion);

    /** Jugada sin elegir promoción: si un peón llega a la última fila, se convierte en dama. */
    default void intentarMovimiento(Posicion origen, Posicion destino) {
        intentarMovimiento(origen, destino, Promocion.DAMA);
    }

    void deshacerJugada();

    void rehacerJugada();

    Color turnoActual();

    boolean estaTerminada();

    /**
     * Vacío si en el origen hay una pieza del jugador de turno. Permite avisar antes de pedir el
     * destino.
     */
    Optional<String> motivoParaNoMoverDesde(Posicion origen);

    /**
     * true si la jugada es legal y lleva un peón a la última fila: la interfaz pregunta entonces la
     * pieza.
     */
    boolean requierePromocion(Posicion origen, Posicion destino);
}
