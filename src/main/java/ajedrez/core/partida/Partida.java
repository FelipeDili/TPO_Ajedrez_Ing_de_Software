package ajedrez.core.partida;

import ajedrez.core.comando.ComandoMoverPieza;
import ajedrez.core.fabrica.Promocion;
import ajedrez.core.fabrica.TableroFactory;
import ajedrez.core.modelo.Color;
import ajedrez.core.modelo.Pieza;
import ajedrez.core.modelo.Posicion;
import ajedrez.core.modelo.Tablero;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Orquesta la partida: tablero, turno, historial para deshacer y rehacer, y la fase actual (State).
 */
public class Partida implements EntradaAjedrez {

    /** 100 medios movimientos = 50 movimientos de cada bando. */
    private static final int LIMITE_ESTANDAR_MEDIOS_MOVIMIENTOS = 100;

    /** Lo que pidió el jugador: alcanza para volver a jugarla al rehacer. */
    private record Jugada(Posicion origen, Posicion destino, Promocion promocion) {
    }

    private record Registro(Jugada jugada, ComandoMoverPieza comando, int relojPrevio,
                            String clavePosicion) {
    }

    private final Tablero tablero;
    private final NotificadorUI notificador;
    private final ValidadorDeJugadas validador;
    private final int limiteMedioMovimientos;
    private final Deque<Registro> historial = new ArrayDeque<>();
    private final Deque<Jugada> deshechas = new ArrayDeque<>();
    private final Map<String, Integer> repeticiones = new HashMap<>();

    private Color turnoActual = Color.BLANCAS;
    private EstadoPartida estado = new EnCurso();
    private int medioMovimientosSinProgreso = 0;

    public Partida(NotificadorUI notificador, TableroFactory factory) {
        this(notificador, factory, new ValidadorDeJugadas(), LIMITE_ESTANDAR_MEDIOS_MOVIMIENTOS);
    }

    public Partida(NotificadorUI notificador, TableroFactory factory,
                   ValidadorDeJugadas validador, int limiteMedioMovimientos) {
        this.notificador = Objects.requireNonNull(notificador);
        this.validador = Objects.requireNonNull(validador);
        this.tablero = Objects.requireNonNull(factory).crearTableroInicial();
        this.limiteMedioMovimientos = limiteMedioMovimientos;
        repeticiones.put(clavePosicionActual(), 1);
    }

    @Override
    public void iniciar() {
        notificador.alTableroActualizado(tablero);
    }

    @Override
    public void intentarMovimiento(Posicion origen, Posicion destino, Promocion promocion) {
        int jugadasAntes = historial.size();
        estado.intentarMovimiento(this, origen, destino, Objects.requireNonNull(promocion));
        if (historial.size() > jugadasAntes) {
            deshechas.clear(); // una jugada nueva descarta lo que se había deshecho
        }
    }

    @Override
    public Optional<String> motivoParaNoMoverDesde(Posicion origen) {
        if (estado.estaTerminada()) {
            return Optional.of("La partida terminó: " + estado.descripcion());
        }
        return validador.motivoDeOrigenInvalido(tablero, turnoActual, origen);
    }

    @Override
    public boolean requierePromocion(Posicion origen, Posicion destino) {
        return !estado.estaTerminada()
                && validador.esLegal(tablero, turnoActual, origen, destino)
                && tablero.obtenerPieza(origen).promocionaEn(destino, tablero);
    }

    /** Se puede deshacer en cualquier fase, incluso después de un jaque mate. */
    @Override
    public void deshacerJugada() {
        if (historial.isEmpty()) {
            notificador.alError("No hay jugadas para deshacer");
            return;
        }
        Registro registro = historial.pop();
        repeticiones.merge(registro.clavePosicion(), -1, Integer::sum);
        registro.comando().deshacer();
        medioMovimientosSinProgreso = registro.relojPrevio();
        turnoActual = turnoActual.opuesto();
        estado = new EnCurso();
        deshechas.push(registro.jugada());
        notificador.alTableroActualizado(tablero);
    }

    /**
     * Vuelve a jugar la última jugada deshecha. Pasa por la fase actual como cualquier jugada, así
     * se vuelven a evaluar el jaque, el mate y las tablas.
     */
    @Override
    public void rehacerJugada() {
        if (deshechas.isEmpty()) {
            notificador.alError("No hay jugadas para rehacer");
            return;
        }
        Jugada jugada = deshechas.pop();
        estado.intentarMovimiento(this, jugada.origen(), jugada.destino(), jugada.promocion());
    }

    public Tablero tablero() {
        return tablero;
    }

    @Override
    public Color turnoActual() {
        return turnoActual;
    }

    @Override
    public boolean estaTerminada() {
        return estado.estaTerminada();
    }

    public EstadoPartida estado() {
        return estado;
    }

    // ---- Colaboración con las fases (mismo paquete) ----

    NotificadorUI notificador() {
        return notificador;
    }

    ValidadorDeJugadas validador() {
        return validador;
    }

    int limiteMedioMovimientos() {
        return limiteMedioMovimientos;
    }

    int medioMovimientosSinProgreso() {
        return medioMovimientosSinProgreso;
    }

    int repeticionesDePosicionActual() {
        return repeticiones.getOrDefault(clavePosicionActual(), 0);
    }

    void cambiarEstado(EstadoPartida nuevo) {
        this.estado = nuevo;
    }

    /** Ejecuta una jugada ya validada: mueve, cambia el turno y actualiza los contadores. */
    void aplicarJugada(Posicion origen, Posicion destino, Promocion promocion) {
        ComandoMoverPieza comando = new ComandoMoverPieza(tablero, origen, destino, promocion);
        int relojPrevio = medioMovimientosSinProgreso;
        comando.ejecutar();

        boolean hayProgreso = comando.piezaCapturada() != null
                || comando.piezaMovida().esMovimientoIrreversible();
        medioMovimientosSinProgreso = hayProgreso ? 0 : medioMovimientosSinProgreso + 1;

        turnoActual = turnoActual.opuesto();
        String clave = clavePosicionActual();
        repeticiones.merge(clave, 1, Integer::sum);
        historial.push(new Registro(new Jugada(origen, destino, promocion), comando, relojPrevio, clave));
    }

    /**
     * Dos posiciones solo cuentan como repetidas si además tienen los mismos derechos de enroque
     * (reglamento FIDE): por eso la clave suma dónde quedan reyes y torres que nunca se movieron.
     */
    private String clavePosicionActual() {
        StringBuilder clave = new StringBuilder(turnoActual + "|" + tablero.clave() + "|");
        for (Color color : Color.values()) {
            for (Posicion pos : tablero.posicionesDe(color)) {
                Pieza pieza = tablero.obtenerPieza(pos);
                if ((pieza.esRey() && !pieza.seHaMovido()) || pieza.puedeEnrocar()) {
                    clave.append(pos);
                }
            }
        }
        return clave.toString();
    }
}
