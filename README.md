# Ajedrez en Java — TPO Ingeniería de Software

## Cómo correrlo

Requiere JDK 17+ y Maven.

```
mvn test                                       # tests del núcleo
mvn compile
java -cp target/classes ajedrez.consola.Main   # jugar por consola (colores + Unicode)
java -cp target/classes ajedrez.consola.Main --ascii   # versión simple, sin colores
```

Al arrancar aparece un menú para empezar la partida o cambiar el tema (colores o ASCII).

Jugadas: `mover` (pide origen y después destino) o `e2 e4` directo. Para enrocar, mover el rey
dos casillas hacia la torre (`e1 g1`). Comandos: `deshacer`, `rehacer`, `ayuda`, `salir`
(vuelve al menú).

Sin Maven: `bash correr-tests.sh` compila y corre los tests con el JUnit standalone de `lib/`
(bajarlo de Maven Central: `junit-platform-console-standalone-1.10.2.jar`).

### Windows (Windows Terminal, PowerShell, cmd)

- Antes de ejecutar, poné la consola en UTF-8 para que se vean las piezas: `chcp 65001`.
- Los colores funcionan en Windows Terminal y en PowerShell de Windows 10/11. Si aparecen
  caracteres raros como `←[48;5;137m`, tu consola no interpreta colores ANSI: usá `--ascii`.
- Si las piezas se ven como cuadrados, la fuente de la terminal no tiene los símbolos de ajedrez
  (probá "Consolas" o "Segoe UI Symbol") o usá `--ascii`.
- En el modo `--ascii`: mayúsculas = blancas, minúsculas = negras. P peón, T torre, C caballo,
  A alfil, D dama, R rey.

## Arquitectura

```
ajedrez.core            <- NÚCLEO: sin UI ni frameworks (solo java.*)
  modelo                Color, Posicion, Pieza, Tablero, MovimientoEstrategia
  movimiento            una estrategia por pieza + bloques reutilizables
  fabrica               PiezaFactory, TableroFactory, TableroEstandarFactory
  comando               ComandoMovimiento, ComandoMoverPieza (deshacer, promoción y enroque)
  partida               Partida, EntradaAjedrez, NotificadorUI, fases (State), ValidadorDeJugadas
ajedrez.consola         <- ADAPTADOR: ConsolaUI (salida, implementa NotificadorUI),
                        PartidaConsola (entrada: lee comandos y conversa con el jugador),
                        MenuConsola, Tema y Main (solo arma las piezas)
```

La dependencia va siempre de la consola hacia el núcleo, nunca al revés.

| Patrón / principio | Dónde |
|---|---|
| Strategy + composición sobre herencia | `Pieza` recibe un `MovimientoEstrategia`; no hay una subclase por pieza |
| Composite | `MovimientoCompuesto` (la reina es torre + alfil) |
| Command | `ComandoMoverPieza`: deshacer/rehacer y también simular jugadas para validar jaque |
| State | `EnCurso`, `JaqueMate`, `Tablas` (fases de la partida) |
| Observer / puerto de salida | `NotificadorUI` |
| Puerto de entrada | `EntradaAjedrez` |
| Factory + inyección de dependencias | `TableroFactory` se inyecta en `Partida` |

## Alcance

Obligatorio: tablero, 6 piezas, turnos, captura, movimientos inválidos, jaque.
Opcional implementado: jaque mate, tablas (ahogado, triple repetición, 50 movimientos),
deshacer y rehacer, enroque, promoción de peón (el jugador elige dama, torre, alfil o caballo).
No implementado: captura al paso, IA, interfaz gráfica.

## Cómo agregar una pieza nueva (para la defensa)

Alcanza con una `MovimientoEstrategia` (puede ser una lambda) y crear la `Pieza` con ella.
Ver `ExtensibilidadTest`. No se modifica ninguna clase existente. Cambiar el tamaño del
tablero es pasar otras dimensiones a `new Tablero(filas, columnas)` desde una `TableroFactory`.
