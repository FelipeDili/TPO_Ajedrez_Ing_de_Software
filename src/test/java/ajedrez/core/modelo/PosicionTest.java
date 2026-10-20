package ajedrez.core.modelo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PosicionTest {

    @ParameterizedTest
    @CsvSource({"e2, 1, 4", "a1, 0, 0", "h8, 7, 7"})
    void convierteNotacionAlgebraicaACoordenadas(String notacion, int fila, int columna) {
        Posicion posicion = Posicion.de(notacion);

        assertEquals(new Posicion(fila, columna), posicion);
    }

    @Test
    void aceptaMayusculasYEspacios() {
        Posicion posicion = Posicion.de(" E2 ");

        assertEquals(new Posicion(1, 4), posicion);
    }

    @ParameterizedTest
    @CsvSource({"0, 0, a1", "7, 7, h8"})
    void toStringDevuelveLaNotacion(int fila, int columna, String notacion) {
        String texto = new Posicion(fila, columna).toString();

        assertEquals(notacion, texto);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "e", "22", "ex"})
    void rechazaNotacionInvalida(String notacion) {
        assertThrows(IllegalArgumentException.class, () -> Posicion.de(notacion));
    }

    @Test
    void desplazarDevuelveUnaPosicionNuevaSinModificarLaOriginal() {
        Posicion origen = Posicion.de("d4");

        Posicion destino = origen.desplazar(2, 1);

        assertEquals(Posicion.de("e6"), destino);
        assertEquals(Posicion.de("d4"), origen);
    }
}
