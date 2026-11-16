#!/usr/bin/env bash
# Compila y corre el ajedrez por consola. Solo necesita un JDK 17+ (no hace falta Maven).
# Uso: bash jugar.sh          (colores + Unicode)
#      bash jugar.sh --ascii  (versión simple, sin colores)
cd "$(dirname "$0")" || exit 1
rm -rf target/classes
mkdir -p target/classes
javac -encoding UTF-8 -sourcepath src/main/java -d target/classes src/main/java/ajedrez/consola/Main.java || exit 1
java -Dstdout.encoding=UTF-8 -cp target/classes ajedrez.consola.Main "$@"
