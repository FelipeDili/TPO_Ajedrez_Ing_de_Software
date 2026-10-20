package ajedrez.core.modelo;

/**
 * Coordenada de una casilla. Fila 0 = fila "1" del ajedrez (lado de las blancas),
 * columna 0 = columna "a". Es inmutable, así que se puede pasar y comparar sin riesgo.
 */
public record Posicion(int fila, int columna) {

    /** Crea una posición desde notación algebraica, por ejemplo "e2". */
    public static Posicion de(String notacion) {
        if (notacion == null) {
            throw new IllegalArgumentException("La notación no puede ser nula");
        }
        String n = notacion.trim().toLowerCase();
        if (n.length() < 2) {
            throw new IllegalArgumentException("Notación inválida: '" + notacion + "'");
        }
        char letra = n.charAt(0);
        if (letra < 'a' || letra > 'z') {
            throw new IllegalArgumentException("Notación inválida: '" + notacion + "'");
        }
        int numero;
        try {
            numero = Integer.parseInt(n.substring(1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Notación inválida: '" + notacion + "'");
        }
        return new Posicion(numero - 1, letra - 'a');
    }

    public Posicion desplazar(int deltaFila, int deltaColumna) {
        return new Posicion(fila + deltaFila, columna + deltaColumna);
    }

    @Override
    public String toString() {
        return (char) ('a' + columna) + String.valueOf(fila + 1);
    }
}
