package ajedrez.consola;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** El menú se maneja con una entrada armada en memoria, sin terminal real. */
class MenuConsolaTest {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    private MenuConsola menu(String entrada) {
        return new MenuConsola(new BufferedReader(new StringReader(entrada)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8), Tema.UNICODE_COLOR);
    }

    @Test
    void sinCambiosEmpiezaConElTemaInicial() throws IOException {
        MenuConsola m = menu("1\n");

        assertTrue(m.elegir());
        assertEquals(Tema.UNICODE_COLOR, m.tema());
    }

    @Test
    void permiteCambiarElTema() throws IOException {
        MenuConsola m = menu("2\n1\n");

        assertTrue(m.elegir());
        assertEquals(Tema.ASCII, m.tema());
    }

    @Test
    void cambiarDosVecesElTemaVuelveAlInicial() throws IOException {
        MenuConsola m = menu("2\n2\n1\n");

        assertTrue(m.elegir());
        assertEquals(Tema.UNICODE_COLOR, m.tema());
    }

    @Test
    void salirOCerrarLaEntradaNoEmpiezaPartida() throws IOException {
        assertFalse(menu("0\n").elegir());
        assertFalse(menu("").elegir());
    }

    @Test
    void unaOpcionInvalidaAvisaYVuelveAPreguntar() throws IOException {
        MenuConsola m = menu("9\n1\n");

        assertTrue(m.elegir());
        assertTrue(buffer.toString(StandardCharsets.UTF_8).contains("Opción inválida"));
    }
}
