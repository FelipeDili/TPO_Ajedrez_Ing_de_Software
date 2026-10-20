package ajedrez.consola;

/** Cómo se dibuja la consola. Se elige en el menú (o con --ascii al arrancar). */
public enum Tema {

    /** Casillas de colores (ANSI 256) y piezas con símbolos Unicode. Es el tema por defecto. */
    UNICODE_COLOR,

    /** Letras y puntos, sin códigos de escape: funciona en cualquier terminal. */
    ASCII
}
