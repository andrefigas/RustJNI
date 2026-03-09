#!/bin/bash
# ============================================================
# run-browser.sh — Opens Flappy Bird (WASM) in the browser
# Starts a local HTTP server (Python) and opens the browser.
# Press Ctrl+C to stop the server.
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
WWW_DIR="$SCRIPT_DIR/wasm/app/src/main/assets/www"

if [ ! -f "$WWW_DIR/index.html" ]; then
    echo "ERROR: index.html not found in $WWW_DIR"
    echo "Make sure the wasm module has been built first."
    exit 1
fi

echo "Starting local server on http://localhost:8080 ..."
echo "Serving from: $WWW_DIR"
echo "Press Ctrl+C to stop."
echo

# Open browser (cross-platform)
if command -v xdg-open &>/dev/null; then
    xdg-open "http://localhost:8080" &
elif command -v open &>/dev/null; then
    open "http://localhost:8080" &
fi

cd "$WWW_DIR"
python3 -c "
import http.server
import socketserver

class WasmHandler(http.server.SimpleHTTPRequestHandler):
    extensions_map = {
        '.html': 'text/html',
        '.js': 'application/javascript',
        '.mjs': 'application/javascript',
        '.wasm': 'application/wasm',
        '.css': 'text/css',
        '.json': 'application/json',
        '.png': 'image/png',
        '.jpg': 'image/jpeg',
        '.svg': 'image/svg+xml',
        '': 'application/octet-stream',
    }

with socketserver.TCPServer(('', 8080), WasmHandler) as httpd:
    print('Serving on http://localhost:8080')
    httpd.serve_forever()
"
