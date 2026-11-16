# Ajedrez en Java — TPO Ingeniería de Software

## Para probarlo rápido

Con una terminal abierta en esta carpeta y Java 17 o superior instalado, alcanza con:

```
java -jar ejecutable/ajedrez.jar
```

Si la consola no muestra bien las piezas o los colores, usar `java -jar ejecutable/ajedrez.jar --ascii`.
Si el jar no arranca, `bash jugar.sh` (Linux/macOS) o `jugar.bat` (Windows) compilan y corren solos.
Dentro del programa: opción `1` para empezar y jugadas como `e2 e4`.

## Cómo correrlo (detalle)

Requiere JDK 17 o superior (verificar con `java -version`). Hay tres formas, de la más simple a la más completa:

1. **Jar ejecutable (sin compilar):**
   ```
   java -jar ejecutable/ajedrez.jar            # colores + Unicode
   java -jar ejecutable/ajedrez.jar --ascii    # versión simple, sin colores
   ```
2. **Compilando desde el código, sin Maven** (solo necesita el JDK):
   ```
   bash jugar.sh          # Linux / macOS / Git Bash
   jugar.bat              # Windows (cmd o PowerShell)
   ```
   Ambos aceptan `--ascii`.
3. **Con Maven:**
   ```
   mvn test                                          # tests del núcleo
   mvn package                                       # genera target/ajedrez-1.0.0.jar
   java -jar target/ajedrez-1.0.0.jar
   ```

Al arrancar aparece un menú para empezar la partida o cambiar el tema (colores o ASCII).

Jugadas: `mover` (pide origen y después destino) o `e2 e4` directo. Para enrocar, mover el rey
dos casillas hacia la torre (`e1 g1`). Comandos: `deshacer`, `rehacer`, `ayuda`, `salir`
(vuelve al menú).

Sin Maven: `bash correr-tests.sh` compila y corre los tests con el JUnit standalone de `lib/`
(bajarlo de Maven Central: `junit-platform-console-standalone-1.10.2.jar`).

### Windows (Windows Terminal, PowerShell, cmd)

- El programa pone la consola en UTF-8 al arrancar (equivale a `chcp 65001`), así que no hace falta
  hacerlo a mano.
- Los colores funcionan en Windows Terminal y en PowerShell de Windows 10/11. Si aparecen
  caracteres raros como `←[48;5;137m`, tu consola no interpreta colores ANSI: usá `--ascii`.
- Si las piezas se ven como cuadrados, la fuente de la terminal no tiene los símbolos de ajedrez
  (probá "Consolas" o "Segoe UI Symbol") o usá `--ascii`.
- Con colores, las piezas blancas se dibujan huecas (♔) y las negras rellenas (♚). En la terminal de
  VS Code, si las piezas cambian de color según la casilla, poné
  `"terminal.integrated.minimumContrastRatio": 1` en la configuración.
- En el modo `--ascii`: mayúsculas = blancas, minúsculas = negras. P peón, T torre, C caballo,
  A alfil, D dama, R rey.

## Arquitectura

```
ajedrez.core            <- NÚCLEO: sin UI ni frameworks (solo java.*)
  modelo                Color, Posicion, Pieza, Tablero, MovimientoEstrategia
  movimiento            una estrategia por pieza + bloques reutilizables
  fabrica               PiezaFactory, TableroFactory, TableroEstandarFactory
  comando               ComandoMoverPieza (deshacer, promoción y enroque)
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
| Puerto de entrada | `EntradaAjedrez`: acciones y consultas; la consola depende solo de este puerto |
| Factory + inyección de dependencias | `TableroFactory` se inyecta en `Partida` |

## Diagrama de clases (UML)

En [`uml/diagrama-de-clases.pdf`](uml/diagrama-de-clases.pdf), en dos páginas: la arquitectura
(consola, partida, comandos y fábricas) y el modelo con las estrategias de movimiento.
Se genera desde los archivos `.puml` de la misma carpeta con [PlantUML](https://plantuml.com);
si cambia el código, se editan esos archivos en lugar de redibujar el diagrama.

## Alcance

Obligatorio: tablero, 6 piezas, turnos, captura, movimientos inválidos, jaque.
Opcional implementado: jaque mate, tablas (ahogado, triple repetición, 50 movimientos),
deshacer y rehacer, enroque, promoción de peón (el jugador elige dama, torre, alfil o caballo).
No implementado: captura al paso, IA, interfaz gráfica.

## Cómo agregar una pieza nueva (para la defensa)

Alcanza con una `MovimientoEstrategia` (puede ser una lambda) y crear la `Pieza` con ella.
Ver `ExtensibilidadTest`. No se modifica ninguna clase existente. Cambiar el tamaño del
tablero es pasar otras dimensiones a `new Tablero(filas, columnas)` desde una `TableroFactory`.
