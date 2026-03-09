@echo off
REM ============================================================
REM clean-generated.bat — Remove all auto-generated files
REM from so, wasm, standalone modules and Rust build caches
REM ============================================================

echo ============================================================
echo  Cleaning auto-generated files...
echo ============================================================
echo.

REM --- SO module: clean markers in MainActivity.kt ---
set "SO_MAIN_KT=so\app\src\main\java\com\example\flappybirdnative\MainActivity.kt"
if exist "%SO_MAIN_KT%" (
    echo [SO] Cleaning RustJNI markers in MainActivity.kt...
    powershell -Command "(Get-Content '%SO_MAIN_KT%' -Raw) -replace '(?s)\r?\n?[ \t]*//<RustJNI>.*?//</RustJNI>[ \t]*\r?\n?', \"`n\" | Set-Content '%SO_MAIN_KT%' -NoNewline"
)

REM --- SO module: Rust build cache ---
echo [SO] Removing Rust build cache...
if exist "so\app\src\main\rust\target" rmdir /s /q "so\app\src\main\rust\target"

REM --- Standalone module: generated bridge crate + WASM binary ---
echo [STANDALONE] Removing auto-generated WASM bridge crate...
if exist "standalone\wasm" rmdir /s /q "standalone\wasm"
echo [STANDALONE] Removing generated WASM binary...
if exist "standalone\app\src\main\assets\flappy.wasm" del /q "standalone\app\src\main\assets\flappy.wasm"

REM --- Standalone module: clean markers in MainActivity.kt ---
set "MAIN_KT=standalone\app\src\main\java\com\example\flappybirdstandalone\MainActivity.kt"
if exist "%MAIN_KT%" (
    echo [STANDALONE] Cleaning RustWasm markers in MainActivity.kt...
    powershell -Command "(Get-Content '%MAIN_KT%' -Raw) -replace '(?s)\r?\n?[ \t]*//<RustWasm-imports>.*?//</RustWasm-imports>[ \t]*\r?\n?', \"`n\" | Set-Content '%MAIN_KT%' -NoNewline"
    powershell -Command "(Get-Content '%MAIN_KT%' -Raw) -replace '(?s)\r?\n?[ \t]*//<RustWasm>.*?//</RustWasm>[ \t]*\r?\n?', \"`n\" | Set-Content '%MAIN_KT%' -NoNewline"
)

REM --- Core Rust build cache ---
echo [CORE] Removing Rust build cache...
if exist "rust\target" rmdir /s /q "rust\target"

echo.
echo ============================================================
echo  Done! All auto-generated files removed.
echo ============================================================
pause
