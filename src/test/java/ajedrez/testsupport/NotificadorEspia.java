package ajedrez.testsupport;

import ajedrez.core.modelo.Color;
import ajedrez.core.modelo.Tablero;
import ajedrez.core.partida.NotificadorUI;

import java.util.ArrayList;
import java.util.List;

/** Notificador de prueba: solo registra lo que el núcleo le avisa, sin dibujar nada. */
public class NotificadorEspia implements NotificadorUI {

    public final List<String> errores = new ArrayList<>();
    public final List<Color> jaques = new ArrayList<>();
    public final List<String> finales = new ArrayList<>();
    public int actualizaciones = 0;

    @Override
    public void alTableroActualizado(Tablero tablero) {
        actualizaciones++;
    }

    @Override
    public void alJaque(Color colorEnPeligro) {
        jaques.add(colorEnPeligro);
    }

    @Override
    public void alError(String mensaje) {
        errores.add(mensaje);
    }

    @Override
    public void alFinDePartida(String resultado) {
        finales.add(resultado);
    }
}
