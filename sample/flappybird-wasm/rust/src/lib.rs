// ============================================================
// Flappy Bird - Core Game Logic (platform-agnostic, simplified POC)
// ============================================================

pub const CANVAS_WIDTH: f64 = 360.0;
pub const CANVAS_HEIGHT: f64 = 640.0;

pub const BIRD_X: f64 = 80.0;
pub const BIRD_RADIUS: f64 = 15.0;
pub const GRAVITY: f64 = 0.4;
pub const JUMP_FORCE: f64 = -7.0;
pub const MAX_VELOCITY: f64 = 10.0;

pub const PIPE_WIDTH: f64 = 52.0;
pub const PIPE_GAP: f64 = 150.0;
pub const PIPE_SPEED: f64 = 2.5;
pub const PIPE_SPAWN_SECONDS: f64 = 100.0 / 60.0;

pub const GROUND_HEIGHT: f64 = 60.0;
pub const GROUND_Y: f64 = CANVAS_HEIGHT - GROUND_HEIGHT;

// ============================================================
// Game State
// ============================================================

#[derive(PartialEq, Clone, Copy)]
pub enum State {
    WaitingToStart,
    Playing,
    GameOver,
}

pub struct Bird {
    pub y: f64,
    pub velocity: f64,
}

pub struct Pipe {
    pub x: f64,
    pub gap_y: f64,
}

// ============================================================
// Game
// ============================================================

pub struct Game {
    pub bird: Bird,
    pub pipes: Vec<Pipe>,
    pub state: State,
    pub spawn_timer: f64,
    pub pipe_seed: u32,
}

impl Game {
    pub fn new() -> Game {
        Game {
            bird: Bird {
                y: CANVAS_HEIGHT / 2.0,
                velocity: 0.0,
            },
            pipes: Vec::new(),
            state: State::WaitingToStart,
            spawn_timer: 0.0,
            pipe_seed: 0,
        }
    }

    pub fn on_input(&mut self) {
        match self.state {
            State::WaitingToStart => {
                self.state = State::Playing;
                self.bird.velocity = JUMP_FORCE;
            }
            State::Playing => {
                self.bird.velocity = JUMP_FORCE;
            }
            State::GameOver => {
                self.reset();
            }
        }
    }

    pub fn update(&mut self, dt: f64) {
        if self.state != State::Playing {
            return;
        }

        let f = dt * 60.0;

        // Bird physics
        self.bird.velocity += GRAVITY * f;
        if self.bird.velocity > MAX_VELOCITY {
            self.bird.velocity = MAX_VELOCITY;
        }
        self.bird.y += self.bird.velocity * f;

        // Spawn pipes
        self.spawn_timer += dt;
        if self.spawn_timer >= PIPE_SPAWN_SECONDS {
            self.spawn_timer -= PIPE_SPAWN_SECONDS;
            self.pipe_seed += 1;
            let gap_y = 120.0 + pseudo_random(self.pipe_seed) * (GROUND_Y - 240.0 - PIPE_GAP);
            self.pipes.push(Pipe { x: CANVAS_WIDTH, gap_y });
        }

        // Move pipes
        for pipe in &mut self.pipes {
            pipe.x -= PIPE_SPEED * f;
        }

        // Remove off-screen pipes
        self.pipes.retain(|p| p.x + PIPE_WIDTH > -10.0);

        // Collision
        if self.check_collision() {
            self.state = State::GameOver;
        }
    }

    pub fn reset(&mut self) {
        self.bird.y = CANVAS_HEIGHT / 2.0;
        self.bird.velocity = 0.0;
        self.pipes.clear();
        self.spawn_timer = 0.0;
        self.pipe_seed = 0;
        self.state = State::WaitingToStart;
    }

    pub fn check_collision(&self) -> bool {
        if self.bird.y + BIRD_RADIUS > GROUND_Y || self.bird.y - BIRD_RADIUS < 0.0 {
            return true;
        }

        for pipe in &self.pipes {
            let left = pipe.x;
            let right = pipe.x + PIPE_WIDTH;

            if circle_rect_collision(BIRD_X, self.bird.y, BIRD_RADIUS, left, 0.0, right, pipe.gap_y) {
                return true;
            }

            let bottom_top = pipe.gap_y + PIPE_GAP;
            if circle_rect_collision(BIRD_X, self.bird.y, BIRD_RADIUS, left, bottom_top, right, GROUND_Y) {
                return true;
            }
        }

        false
    }

    pub fn width(&self) -> f64 {
        CANVAS_WIDTH
    }

    pub fn height(&self) -> f64 {
        CANVAS_HEIGHT
    }
}

// ============================================================
// Utility functions
// ============================================================

pub fn circle_rect_collision(
    cx: f64, cy: f64, cr: f64,
    rx1: f64, ry1: f64, rx2: f64, ry2: f64,
) -> bool {
    let closest_x = cx.max(rx1).min(rx2);
    let closest_y = cy.max(ry1).min(ry2);
    let dx = cx - closest_x;
    let dy = cy - closest_y;
    (dx * dx + dy * dy) < (cr * cr)
}

pub fn pseudo_random(seed: u32) -> f64 {
    let mut x = seed;
    x ^= x << 13;
    x ^= x >> 17;
    x ^= x << 5;
    (x % 1000) as f64 / 1000.0
}

// ============================================================
// Standalone API (flat functions for WASM bridge generation)
// ============================================================

// JNI targets use Mutex (thread-safe); WASM is single-threaded, so use static mut.
#[cfg(not(target_arch = "wasm32"))]
mod game_state {
    use super::*;
    use std::sync::{Mutex, LazyLock};

    static GAME: LazyLock<Mutex<Game>> = LazyLock::new(|| Mutex::new(Game::new()));

    pub fn with_game<F: FnOnce(&Game) -> R, R>(f: F) -> R {
        f(&GAME.lock().unwrap())
    }

    pub fn with_game_mut<F: FnOnce(&mut Game) -> R, R>(f: F) -> R {
        f(&mut GAME.lock().unwrap())
    }
}

#[cfg(target_arch = "wasm32")]
mod game_state {
    use super::*;
    use core::cell::UnsafeCell;

    struct WasmGame(UnsafeCell<Option<Game>>);
    unsafe impl Sync for WasmGame {}

    static GAME: WasmGame = WasmGame(UnsafeCell::new(None));

    fn ensure_init() {
        unsafe {
            let ptr = GAME.0.get();
            if (*ptr).is_none() {
                *ptr = Some(Game::new());
            }
        }
    }

    pub fn with_game<F: FnOnce(&Game) -> R, R>(f: F) -> R {
        ensure_init();
        unsafe { f((*GAME.0.get()).as_ref().unwrap()) }
    }

    pub fn with_game_mut<F: FnOnce(&mut Game) -> R, R>(f: F) -> R {
        ensure_init();
        unsafe { f((*GAME.0.get()).as_mut().unwrap()) }
    }
}

use game_state::*;

pub fn game_on_input() {
    with_game_mut(|g| g.on_input());
}

pub fn game_update(dt: f64) {
    with_game_mut(|g| g.update(dt));
}

pub fn game_reset() {
    with_game_mut(|g| g.reset());
}

pub fn game_get_state() -> i32 {
    with_game(|g| match g.state {
        State::WaitingToStart => 0, State::Playing => 1, State::GameOver => 2,
    })
}

pub fn game_get_bird_y() -> f64 {
    with_game(|g| g.bird.y)
}

pub fn game_get_pipe_count() -> i32 {
    with_game(|g| g.pipes.len() as i32)
}

pub fn game_get_pipe_x(index: i32) -> f64 {
    with_game(|g| g.pipes.get(index as usize).map(|p| p.x).unwrap_or(0.0))
}

pub fn game_get_pipe_gap_y(index: i32) -> f64 {
    with_game(|g| g.pipes.get(index as usize).map(|p| p.gap_y).unwrap_or(0.0))
}

pub fn game_get_canvas_width() -> f64 {
    CANVAS_WIDTH
}

pub fn game_get_canvas_height() -> f64 {
    CANVAS_HEIGHT
}
