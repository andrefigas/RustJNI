import init, { WasmGame } from './pkg/flappy_bird.js';

async function main() {
    // Initialize the WASM module
    await init();

    const canvas = document.getElementById('game');
    const ctx = canvas.getContext('2d');
    const game = new WasmGame();

    // Set canvas size from game constants
    canvas.width = game.width();
    canvas.height = game.height();

    // Scale canvas to fit screen while keeping aspect ratio
    function resize() {
        const scaleX = window.innerWidth / canvas.width;
        const scaleY = window.innerHeight / canvas.height;
        const scale = Math.min(scaleX, scaleY);
        canvas.style.width = (canvas.width * scale) + 'px';
        canvas.style.height = (canvas.height * scale) + 'px';
        canvas.style.marginTop = ((window.innerHeight - canvas.height * scale) / 2) + 'px';
    }
    resize();
    window.addEventListener('resize', resize);

    // Input: touch, click, keyboard
    canvas.addEventListener('touchstart', (e) => {
        e.preventDefault();
        game.on_input();
    }, { passive: false });

    canvas.addEventListener('click', () => {
        game.on_input();
    });

    document.addEventListener('keydown', (e) => {
        if (e.code === 'Space' || e.code === 'ArrowUp') {
            e.preventDefault();
            game.on_input();
        }
    });

    // Game loop with delta time (runs at native refresh rate for smoothness)
    let lastTime = 0;

    function loop(timestamp) {
        if (lastTime === 0) lastTime = timestamp;
        const dt = Math.min((timestamp - lastTime) / 1000, 0.05); // seconds, capped at 50ms
        lastTime = timestamp;

        game.update(dt);
        game.render(ctx);
        requestAnimationFrame(loop);
    }
    requestAnimationFrame(loop);
}

main();
