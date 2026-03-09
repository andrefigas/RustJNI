use wasm_bindgen::prelude::*;
use web_sys::CanvasRenderingContext2d;

use flappy_core::{
    Game, State,
    BIRD_RADIUS, BIRD_X, CANVAS_HEIGHT, CANVAS_WIDTH,
    GROUND_HEIGHT, GROUND_Y, PIPE_GAP, PIPE_WIDTH,
};

#[wasm_bindgen]
pub struct WasmGame {
    game: Game,
}

#[wasm_bindgen]
impl WasmGame {
    #[wasm_bindgen(constructor)]
    pub fn new() -> WasmGame {
        WasmGame { game: Game::new() }
    }

    pub fn on_input(&mut self) {
        self.game.on_input();
    }

    pub fn update(&mut self, dt: f64) {
        self.game.update(dt);
    }

    pub fn render(&self, ctx: &CanvasRenderingContext2d) {
        self.draw_background(ctx);
        self.draw_pipes(ctx);
        self.draw_ground(ctx);
        self.draw_bird(ctx);
    }

    pub fn width(&self) -> f64 {
        self.game.width()
    }

    pub fn height(&self) -> f64 {
        self.game.height()
    }
}

impl WasmGame {
    fn draw_background(&self, ctx: &CanvasRenderingContext2d) {
        ctx.set_fill_style_str("#70c5ce");
        ctx.fill_rect(0.0, 0.0, CANVAS_WIDTH, CANVAS_HEIGHT);
    }

    fn draw_ground(&self, ctx: &CanvasRenderingContext2d) {
        ctx.set_fill_style_str("#8B4513");
        ctx.fill_rect(0.0, GROUND_Y, CANVAS_WIDTH, GROUND_HEIGHT);
    }

    fn draw_pipes(&self, ctx: &CanvasRenderingContext2d) {
        for pipe in &self.game.pipes {
            ctx.set_fill_style_str("#73bf2e");
            ctx.fill_rect(pipe.x, 0.0, PIPE_WIDTH, pipe.gap_y);

            let bottom_top = pipe.gap_y + PIPE_GAP;
            let bottom_h = GROUND_Y - bottom_top;
            ctx.fill_rect(pipe.x, bottom_top, PIPE_WIDTH, bottom_h);
        }
    }

    fn draw_bird(&self, ctx: &CanvasRenderingContext2d) {
        ctx.begin_path();
        ctx.set_fill_style_str("#f5c842");
        ctx.arc(BIRD_X, self.game.bird.y, BIRD_RADIUS, 0.0, std::f64::consts::PI * 2.0)
            .unwrap_or(());
        ctx.fill();
    }
}
