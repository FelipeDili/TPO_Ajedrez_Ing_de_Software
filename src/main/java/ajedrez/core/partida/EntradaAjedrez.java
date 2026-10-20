package ajedrez.core.partida;

import ajedrez.core.fabrica.Promocion;
import ajedrez.core.modelo.Posicion;

/** Puerto de entrada: lo que cualquier interfaz (consola, gráfica, web) puede pedirle al núcleo. */
public interface EntradaAjedrez {

    /** Si la jugada lleva un peón a la última fila, se convierte en la pieza indicada. */
    void intentarMovimiento(Posicion origen, Posicion destino, Promocion promocion);

    /** Jugada sin elegir promoción: si un peón llega a la última fila, se convierte en dama. */
    default void intentarMovimiento(Posicion origen, Posicion destino) {
        intentarMovimiento(origen, destino, Promocion.DAMA);
    }

    void deshacerJugada();
}
