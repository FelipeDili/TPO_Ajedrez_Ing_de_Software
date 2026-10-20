#!/usr/bin/env bash
# Compila y corre los tests sin Maven, usando el JUnit standalone de lib/.
set -e
JDK="${JAVA_HOME:-$HOME/.jdks/openjdk-24}/bin"
JUNIT=lib/junit-platform-console-standalone-1.10.2.jar
rm -rf target/classes target/test-classes
"$JDK/javac" -encoding UTF-8 -d target/classes $(find src/main/java -name '*.java')
"$JDK/javac" -encoding UTF-8 -cp "target/classes;$JUNIT" -d target/test-classes $(find src/test/java -name '*.java')
"$JDK/java" -Dstdout.encoding=UTF-8 -jar "$JUNIT" execute --class-path "target/classes;target/test-classes" --scan-class-path --details=tree --disable-banner
