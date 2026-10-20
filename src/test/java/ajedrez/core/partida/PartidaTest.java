package ajedrez.core.partida;

import ajedrez.core.fabrica.PiezaFactory;
import ajedrez.core.fabrica.TableroEstandarFactory;
import ajedrez.core.modelo.Color;
import ajedrez.core.modelo.Pieza;
import ajedrez.core.modelo.Posicion;
import ajedrez.core.modelo.Tablero;
import ajedrez.testsupport.NotificadorEspia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Optional;

import static ajedrez.testsupport.Tableros.factory;
import static ajedrez.testsupport.Tableros.poner;
import static ajedrez.testsupport.Tableros.vacio;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Reglas del juego probadas a través de la Partida, sin ninguna interfaz real. */
class PartidaTest {

    private NotificadorEspia espia;
    private Partida partida;

    @BeforeEach
    void iniciarPartidaEstandar() {
        espia = new NotificadorEspia();
        partida = new Partida(espia, new TableroEstandarFactory());
    }

    private void jugar(String origen, String destino) {
        jugar(partida, origen, destino);
    }

    private void jugar(Partida p, String origen, String destino) {
        p.intentarMovimiento(Posicion.de(origen), Posicion.de(destino));
    }

    private Pieza en(String casilla) {
        return partida.tablero().obtenerPieza(Posicion.de(casilla));
    }

    private Partida partidaCon(Tablero tablero) {
        return new Partida(espia, factory(tablero));
    }

    /** Solo los dos reyes, con un límite chico para la regla de los 50 movimientos. */
    private Partida partidaConReyesYLimite(int limite) {
        Tablero t = vacio();
        poner(t, "e1", PiezaFactory.rey(Color.BLANCAS));
        poner(t, "e8", PiezaFactory.rey(Color.NEGRAS));
        poner(t, "a2", PiezaFactory.peon(Color.BLANCAS));
        return new Partida(espia, factory(t), new ValidadorDeJugadas(), limite);
    }

    private void mateDelLoco() {
        jugar("f2", "f3");
        jugar("e7", "e5");
        jugar("g2", "g4");
        jugar("d8", "h4");
    }

    // ---------- Tablero inicial y turnos ----------

    @Test
    void laPartidaEstandarArrancaConLas32PiezasEnUnTableroDe8x8() {
        Tablero tablero = partida.tablero();

        assertEquals(16, tablero.posicionesDe(Color.BLANCAS).size());
        assertEquals(16, tablero.posicionesDe(Color.NEGRAS).size());
        assertEquals(8, tablero.filas());
        assertEquals(8, tablero.columnas());
    }

    @Test
    void empiezanLasBlancas() {
        assertEquals(Color.BLANCAS, partida.turnoActual());
    }

    @Test
    void losTurnosAlternanTrasCadaJugadaValida() {
        jugar("e2", "e4");

        jugar("e7", "e5");

        assertEquals(Color.BLANCAS, partida.turnoActual());
        assertTrue(espia.errores.isEmpty());
    }

    @Test
    void lasNegrasNoPuedenMoverPrimero() {
        jugar("e7", "e5");

        assertEquals(1, espia.errores.size());
        assertEquals(Color.BLANCAS, partida.turnoActual());
        assertNotNull(en("e7"));
        assertNull(en("e5"));
    }

    @Test
    void notificaElTableroActualizadoDespuesDeUnaJugadaValida() {
        jugar("e2", "e4");

        assertEquals(1, espia.actualizaciones);
    }

    // ---------- Validación de movimientos inválidos ----------

    @ParameterizedTest
    @CsvSource({
            "a1, a5", // torre bloqueada por su propio peón
            "e2, e5", // peón avanzando tres casillas
            "b1, b3", // caballo moviéndose como torre
            "c1, c3", // alfil moviéndose en línea recta
            "d1, d2"  // dama queriendo capturar a su propio peón
    })
    void rechazaMovimientosQueLaPiezaNoPuedeHacer(String origen, String destino) {
        String posicion = partida.tablero().clave();

        jugar(origen, destino);

        assertEquals(1, espia.errores.size());
        assertEquals(posicion, partida.tablero().clave());
        assertEquals(Color.BLANCAS, partida.turnoActual());
    }

    @Test
    void rechazaMoverDesdeUnaCasillaVacia() {
        jugar("e4", "e5");

        assertEquals(List.of("No hay ninguna pieza en e4"), espia.errores);
    }

    @Test
    void rechazaPosicionesFueraDelTablero() {
        partida.intentarMovimiento(new Posicion(0, 0), new Posicion(9, 9));

        assertEquals(List.of("Posición fuera del tablero"), espia.errores);
    }

    // ---------- Origen de una jugada ----------

    @Test
    void sePuedeMoverDesdeUnaPiezaPropia() {
        Optional<String> motivo = partida.motivoParaNoMoverDesde(Posicion.de("e2"));

        assertTrue(motivo.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"e4", "e7"}) // casilla vacía, pieza rival
    void noSePuedeMoverDesdeUnaCasillaSinPiezaPropia(String casilla) {
        Optional<String> motivo = partida.motivoParaNoMoverDesde(Posicion.de(casilla));

        assertTrue(motivo.isPresent());
    }

    @Test
    void noSePuedeMoverDesdeFueraDelTablero() {
        Optional<String> motivo = partida.motivoParaNoMoverDesde(new Posicion(9, 9));

        assertTrue(motivo.isPresent());
    }

    @Test
    void conLaPartidaTerminadaNoSePuedeMoverDesdeNingunaCasilla() {
        mateDelLoco();

        Optional<String> motivo = partida.motivoParaNoMoverDesde(Posicion.de("a2"));

        assertTrue(motivo.isPresent());
    }

    // ---------- Captura ----------

    @Test
    void capturaUnaPiezaRival() {
        jugar("e2", "e4");
        jugar("d7", "d5");

        jugar("e4", "d5");

        assertEquals(Color.BLANCAS, en("d5").color());
        assertEquals(15, partida.tablero().posicionesDe(Color.NEGRAS).size());
        assertEquals(16, partida.tablero().posicionesDe(Color.BLANCAS).size());
    }

    // ---------- Deshacer ----------

    @Test
    void deshacerRestauraLaPiezaCapturada() {
        jugar("e2", "e4");
        jugar("d7", "d5");
        jugar("e4", "d5");

        partida.deshacerJugada();

        assertEquals(16, partida.tablero().posicionesDe(Color.NEGRAS).size());
        assertEquals(Color.NEGRAS, en("d5").color());
        assertEquals(Color.BLANCAS, en("e4").color());
        assertEquals(Color.BLANCAS, partida.turnoActual());
    }

    @Test
    void sePuedeDeshacerVariasJugadasHastaLaPosicionInicial() {
        String posicionInicial = partida.tablero().clave();
        jugar("e2", "e4");
        jugar("e7", "e5");
        jugar("g1", "f3");

        partida.deshacerJugada();
        partida.deshacerJugada();
        partida.deshacerJugada();

        assertEquals(posicionInicial, partida.tablero().clave());
        assertEquals(Color.BLANCAS, partida.turnoActual());
        assertTrue(espia.errores.isEmpty());
    }

    @Test
    void despuesDeDeshacerElPeonConservaSuDoblePaso() {
        jugar("e2", "e4");
        partida.deshacerJugada();

        jugar("e2", "e4"); // fallaría si no se restaurara el contador de movimientos del peón

        assertTrue(espia.errores.isEmpty());
    }

    @Test
    void deshacerSinJugadasInforma() {
        partida.deshacerJugada();

        assertEquals(List.of("No hay jugadas para deshacer"), espia.errores);
    }

    // ---------- Jaque ----------

    @Test
    void detectaYNotificaElJaque() {
        Tablero t = vacio();
        poner(t, "h1", PiezaFactory.rey(Color.BLANCAS));
        poner(t, "a1", PiezaFactory.torre(Color.BLANCAS));
        poner(t, "e8", PiezaFactory.rey(Color.NEGRAS));
        Partida p = partidaCon(t);

        jugar(p, "a1", "a8");

        assertEquals(List.of(Color.NEGRAS), espia.jaques);
        assertFalse(p.estado().estaTerminada()); // el rey negro puede escapar
    }

    @Test
    void unaPiezaClavadaNoPuedeExponerAlRey() {
        Tablero t = vacio();
        poner(t, "e1", PiezaFactory.rey(Color.BLANCAS));
        poner(t, "e2", PiezaFactory.alfil(Color.BLANCAS));
        poner(t, "e8", PiezaFactory.torre(Color.NEGRAS));
        poner(t, "a8", PiezaFactory.rey(Color.NEGRAS));
        Partida p = partidaCon(t);

        jugar(p, "e2", "d3");

        assertEquals(List.of("Ese movimiento deja a tu rey en jaque"), espia.errores);
        assertNotNull(p.tablero().obtenerPieza(Posicion.de("e2")));
        assertEquals(Color.BLANCAS, p.turnoActual());
    }

    @Test
    void elReyNoPuedeMoverseAUnaCasillaAtacada() {
        Tablero t = vacio();
        poner(t, "e1", PiezaFactory.rey(Color.BLANCAS));
        poner(t, "d8", PiezaFactory.torre(Color.NEGRAS));
        poner(t, "h8", PiezaFactory.rey(Color.NEGRAS));
        Partida p = partidaCon(t);

        jugar(p, "e1", "d1"); // columna d atacada por la torre

        assertEquals(1, espia.errores.size());
        assertEquals(Color.BLANCAS, p.turnoActual());
    }

    /** Rey blanco en jaque por la torre de e8; la torre blanca de a2 puede interponerse en e2. */
    private Partida partidaConJaqueQueSePuedeBloquear() {
        Tablero t = vacio();
        poner(t, "e1", PiezaFactory.rey(Color.BLANCAS));
        poner(t, "a2", PiezaFactory.torre(Color.BLANCAS));
        poner(t, "e8", PiezaFactory.torre(Color.NEGRAS));
        poner(t, "h8", PiezaFactory.rey(Color.NEGRAS));
        return partidaCon(t);
    }

    @Test
    void enJaqueNoSePuedeIgnorarElJaque() {
        Partida p = partidaConJaqueQueSePuedeBloquear();

        jugar(p, "a2", "a3");

        assertEquals(1, espia.errores.size());
        assertEquals(Color.BLANCAS, p.turnoActual());
    }

    @Test
    void enJaqueSePuedeBloquearloConOtraPieza() {
        Partida p = partidaConJaqueQueSePuedeBloquear();

        jugar(p, "a2", "e2");

        assertTrue(espia.errores.isEmpty());
        assertEquals(Color.NEGRAS, p.turnoActual());
    }

    // ---------- Jaque mate ----------

    @Test
    void detectaJaqueMate() {
        mateDelLoco();

        JaqueMate mate = assertInstanceOf(JaqueMate.class, partida.estado());
        assertEquals(Color.NEGRAS, mate.ganador());
        assertEquals(1, espia.finales.size());
        assertTrue(espia.jaques.contains(Color.BLANCAS));
        assertTrue(espia.errores.isEmpty());
    }

    @Test
    void conLaPartidaTerminadaSeRechazanLasJugadas() {
        mateDelLoco();
        String posicion = partida.tablero().clave();

        jugar("e2", "e3");

        assertEquals(1, espia.errores.size());
        assertTrue(espia.errores.get(0).contains("terminó"));
        assertEquals(posicion, partida.tablero().clave());
    }

    @Test
    void deshacerElMateVuelveALaPartidaEnCurso() {
        mateDelLoco();

        partida.deshacerJugada();

        assertFalse(partida.estado().estaTerminada());
        assertEquals(Color.NEGRAS, partida.turnoActual());
    }

    // ---------- Tablas ----------

    @Test
    void detectaReyAhogado() {
        Tablero t = vacio();
        poner(t, "b6", PiezaFactory.rey(Color.BLANCAS));
        poner(t, "d7", PiezaFactory.reina(Color.BLANCAS));
        poner(t, "a8", PiezaFactory.rey(Color.NEGRAS));
        Partida p = partidaCon(t);

        jugar(p, "d7", "c7");

        Tablas tablas = assertInstanceOf(Tablas.class, p.estado());
        assertEquals(MotivoTablas.AHOGADO, tablas.motivo());
        assertTrue(espia.jaques.isEmpty()); // ahogado no es jaque
        assertEquals(1, espia.finales.size());
    }

    /** Los caballos salen y vuelven: la posición inicial se repite una vez más. */
    private void idaYVueltaDeCaballos() {
        jugar("g1", "f3");
        jugar("g8", "f6");
        jugar("f3", "g1");
        jugar("f6", "g8");
    }

    @Test
    void detectaTripleRepeticionDeLaPosicion() {
        idaYVueltaDeCaballos();

        idaYVueltaDeCaballos(); // la posición inicial aparece por tercera vez

        Tablas tablas = assertInstanceOf(Tablas.class, partida.estado());
        assertEquals(MotivoTablas.REPETICION, tablas.motivo());
        assertTrue(espia.errores.isEmpty());
    }

    @Test
    void laRepeticionNoSeDeclaraAntesDeLaTerceraVez() {
        idaYVueltaDeCaballos(); // la posición inicial aparece por segunda vez

        assertFalse(partida.estado().estaTerminada());
    }

    @Test
    void alLlegarAlLimiteDeMovimientosSinProgresoHayTablas() {
        Partida p = partidaConReyesYLimite(4);
        jugar(p, "e1", "e2");
        jugar(p, "e8", "e7");
        jugar(p, "e2", "e1");

        jugar(p, "e7", "e8"); // cuarto medio movimiento sin capturas ni peones

        Tablas tablas = assertInstanceOf(Tablas.class, p.estado());
        assertEquals(MotivoTablas.REGLA_DE_LOS_50_MOVIMIENTOS, tablas.motivo());
    }

    @Test
    void antesDelLimiteDeMovimientosSinProgresoNoHayTablas() {
        Partida p = partidaConReyesYLimite(4);
        jugar(p, "e1", "e2");
        jugar(p, "e8", "e7");

        jugar(p, "e2", "e1"); // tercer medio movimiento

        assertFalse(p.estado().estaTerminada());
    }

    @Test
    void unMovimientoDePeonReiniciaElContadorDeLos50Movimientos() {
        Partida p = partidaConReyesYLimite(4);
        jugar(p, "e1", "e2");
        jugar(p, "e8", "e7");
        jugar(p, "a2", "a3"); // peón: reinicia el contador
        jugar(p, "e7", "e8");

        jugar(p, "e2", "e1"); // sin el reinicio, sería el quinto medio movimiento

        assertFalse(p.estado().estaTerminada());
    }

    @Test
    void deshacerRestauraElContadorDeLos50Movimientos() {
        Partida p = partidaConReyesYLimite(4);
        jugar(p, "e1", "e2");
        jugar(p, "e8", "e7");
        jugar(p, "e2", "e1");
        p.deshacerJugada();

        jugar(p, "e2", "e1"); // si el contador no volviera a 2, este sería el cuarto

        assertFalse(p.estado().estaTerminada());
    }
}
