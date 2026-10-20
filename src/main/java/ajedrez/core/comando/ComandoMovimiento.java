package ajedrez.core.comando;

/** Command: una jugada que se puede ejecutar y deshacer. */
public interface ComandoMovimiento {

    void ejecutar();

    void deshacer();
}
