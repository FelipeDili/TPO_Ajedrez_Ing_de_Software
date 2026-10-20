package ajedrez.core.partida;

import ajedrez.core.modelo.Color;
import ajedrez.core.modelo.Tablero;

/**
 * Puerto de salida (Observer): el núcleo avisa lo que pasó sin saber quién escucha ni cómo
 * se muestra. La dependencia apunta hacia el núcleo, no al revés.
 */
public interface NotificadorUI {

    void alTableroActualizado(Tablero tablero);

    void alJaque(Color colorEnPeligro);

    void alError(String mensaje);

    void alFinDePartida(String resultado);
}
