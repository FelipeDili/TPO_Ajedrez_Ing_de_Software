@echo off
REM Compila y corre el ajedrez por consola. Solo necesita un JDK 17+ (no hace falta Maven).
REM Uso: jugar.bat          (colores + Unicode)
REM      jugar.bat --ascii  (version simple, sin colores)
cd /d "%~dp0"
if exist target\classes rmdir /s /q target\classes
mkdir target\classes
javac -encoding UTF-8 -sourcepath src\main\java -d target\classes src\main\java\ajedrez\consola\Main.java
if errorlevel 1 exit /b 1
java -cp target\classes ajedrez.consola.Main %*
