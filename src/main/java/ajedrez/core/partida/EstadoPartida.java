package ajedrez.core.partida;

import ajedrez.core.fabrica.Promocion;
import ajedrez.core.modelo.Posicion;

/**
 * State: fase de la partida. Cada fase decide qué hacer con una jugada pedida:
 * {@link EnCurso} la procesa; {@link JaqueMate} y {@link Tablas} la rechazan.
 */
public interface EstadoPartida {

    void intentarMovimiento(Partida partida, Posicion origen, Posicion destino, Promocion promocion);

    boolean estaTerminada();

    String descripcion();
}
