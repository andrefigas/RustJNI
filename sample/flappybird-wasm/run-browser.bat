@echo off
REM ============================================================
REM run-browser.bat — Opens Flappy Bird (WASM) in Chrome
REM Starts a local HTTP server (Python) and opens the browser.
REM Press Ctrl+C to stop the server.
REM ============================================================

set "WWW_DIR=%~dp0wasm\app\src\main\assets\www"

if not exist "%WWW_DIR%\index.html" (
    echo ERROR: index.html not found in %WWW_DIR%
    echo Make sure the wasm module has been built first.
    pause
    exit /b 1
)

echo Starting local server on http://localhost:8080 ...
echo Serving from: %WWW_DIR%
echo Press Ctrl+C to stop.
echo.

set "SERVER_SCRIPT=%TEMP%\wasm_server.py"
(
echo import http.server
echo import socketserver
echo import os
echo.
echo class WasmHandler(http.server.SimpleHTTPRequestHandler^):
echo     extensions_map = {
echo         '.html': 'text/html',
echo         '.js': 'application/javascript',
echo         '.mjs': 'application/javascript',
echo         '.wasm': 'application/wasm',
echo         '.css': 'text/css',
echo         '.json': 'application/json',
echo         '.png': 'image/png',
echo         '.jpg': 'image/jpeg',
echo         '.svg': 'image/svg+xml',
echo         '': 'application/octet-stream',
echo     }
echo.
echo os.chdir(r"%WWW_DIR%"^)
echo with socketserver.TCPServer(("", 8080^), WasmHandler^) as httpd:
echo     print("Serving on http://localhost:8080"^)
echo     httpd.serve_forever(^)
) > "%SERVER_SCRIPT%"

start "" "http://localhost:8080"
python "%SERVER_SCRIPT%"
