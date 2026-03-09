#!/bin/bash
# ============================================================
# clean-generated.sh — Remove all auto-generated files
# from so, wasm, standalone modules and Rust build caches
# ============================================================

echo "============================================================"
echo " Cleaning auto-generated files..."
echo "============================================================"
echo

# --- SO module: clean markers in MainActivity.kt ---
SO_MAIN_KT="so/app/src/main/java/com/example/flappybirdnative/MainActivity.kt"
if [ -f "$SO_MAIN_KT" ]; then
    echo "[SO] Cleaning RustJNI markers in MainActivity.kt..."
    sed -i -z 's|\n[[:space:]]*//<RustJNI>.*//</RustJNI>[[:space:]]*\n|\n|s' "$SO_MAIN_KT"
fi

# --- SO module: Rust build cache ---
echo "[SO] Removing Rust build cache..."
rm -rf so/app/src/main/rust/target

# --- Standalone module: generated bridge crate + WASM binary ---
echo "[STANDALONE] Removing auto-generated WASM bridge crate..."
rm -rf standalone/wasm
echo "[STANDALONE] Removing generated WASM binary..."
rm -f standalone/app/src/main/assets/flappy.wasm

# --- Standalone module: clean markers in MainActivity.kt ---
MAIN_KT="standalone/app/src/main/java/com/example/flappybirdstandalone/MainActivity.kt"
if [ -f "$MAIN_KT" ]; then
    echo "[STANDALONE] Cleaning RustWasm markers in MainActivity.kt..."
    sed -i -z 's|\n[[:space:]]*//<RustWasm-imports>.*//</RustWasm-imports>[[:space:]]*\n|\n|s' "$MAIN_KT"
    sed -i -z 's|\n[[:space:]]*//<RustWasm>.*//</RustWasm>[[:space:]]*\n|\n|s' "$MAIN_KT"
fi

# --- Core Rust build cache ---
echo "[CORE] Removing Rust build cache..."
rm -rf rust/target

echo
echo "============================================================"
echo " Done! All auto-generated files removed."
echo "============================================================"
