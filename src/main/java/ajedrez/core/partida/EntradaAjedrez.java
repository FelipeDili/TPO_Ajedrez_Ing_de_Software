package ajedrez.core.partida;

import ajedrez.core.fabrica.Promocion;
import ajedrez.core.modelo.Color;
import ajedrez.core.modelo.Posicion;

import java.util.Optional;

/**
 * Puerto de entrada: lo que cualquier interfaz (consola, gráfica, web) puede pedirle al núcleo.
 * Incluye las acciones del jugador y las consultas que una interfaz necesita para guiarlo, así
 * ningún adaptador depende de la clase concreta {@link Partida}.
 */
public interface EntradaAjedrez {

    /** Muestra el estado inicial a quien esté escuchando. */
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
     * Devuelve por qué no se puede mover desde {@code origen}, o vacío si ahí hay una pieza
     * del jugador de turno. Permite que una interfaz avise antes de pedir el destino.
     */
    Optional<String> motivoParaNoMoverDesde(Posicion origen);

    /**
     * true si la jugada es legal y lleva un peón a la última fila. La interfaz lo consulta
     * antes de jugar para preguntarle al jugador en qué pieza convertirlo.
     */
    boolean requierePromocion(Posicion origen, Posicion destino);
}
