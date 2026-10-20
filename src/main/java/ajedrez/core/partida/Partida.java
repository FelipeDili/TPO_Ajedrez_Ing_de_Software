package ajedrez.core.partida;

import ajedrez.core.comando.ComandoMoverPieza;
import ajedrez.core.fabrica.Promocion;
import ajedrez.core.fabrica.TableroFactory;
import ajedrez.core.modelo.Color;
import ajedrez.core.modelo.Posicion;
import ajedrez.core.modelo.Tablero;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Orquestador del juego (el "game manager"). Mantiene el tablero, de quién es el turno, el
 * historial de jugadas para deshacer y la fase actual (State). No conoce ninguna interfaz
 * concreta: habla con el mundo exterior solo a través de {@link EntradaAjedrez} y
 * {@link NotificadorUI}.
 */
public class Partida implements EntradaAjedrez {

    /** 100 medios movimientos = 50 movimientos de cada bando. */
    public static final int LIMITE_ESTANDAR_MEDIOS_MOVIMIENTOS = 100;

    private record Registro(ComandoMoverPieza comando, int relojPrevio, String clavePosicion) {
    }

    private final Tablero tablero;
    private final NotificadorUI notificador;
    private final ValidadorDeJugadas validador;
    private final int limiteMedioMovimientos;
    private final Deque<Registro> historial = new ArrayDeque<>();
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

    /** Muestra el estado inicial a quien esté escuchando. */
    public void iniciar() {
        notificador.alTableroActualizado(tablero);
    }

    @Override
    public void intentarMovimiento(Posicion origen, Posicion destino, Promocion promocion) {
        estado.intentarMovimiento(this, origen, destino, Objects.requireNonNull(promocion));
    }

    /**
     * Devuelve por qué no se puede mover desde {@code origen}, o vacío si ahí hay una pieza
     * del jugador de turno. Permite que una interfaz avise antes de pedir el destino.
     */
    public Optional<String> motivoParaNoMoverDesde(Posicion origen) {
        if (estado.estaTerminada()) {
            return Optional.of("La partida terminó: " + estado.descripcion());
        }
        return validador.motivoDeOrigenInvalido(tablero, turnoActual, origen);
    }

    /**
     * true si la jugada es legal y lleva un peón a la última fila. La interfaz lo consulta
     * antes de jugar para preguntarle al jugador en qué pieza convertirlo.
     */
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
        notificador.alTableroActualizado(tablero);
    }

    public Tablero tablero() {
        return tablero;
    }

    public Color turnoActual() {
        return turnoActual;
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
        historial.push(new Registro(comando, relojPrevio, clave));
    }

    private String clavePosicionActual() {
        return turnoActual + "|" + tablero.clave();
    }
}
