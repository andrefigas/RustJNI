use crate::player_state::PlayerState;
use std::time::{Duration, Instant};

pub struct GameController {
    canvas_width: f32,
    canvas_height: f32,
    player_sprite_width: f32,
    player_sprite_height: f32,
    enemy_sprite_width: f32,
    enemy_sprite_height: f32,

    // Jump and run properties
    player_frame_duration: Duration,
    game_start_time: Option<Instant>,
    player_frames: usize,
    jump_height: f32, // 0.0 to 1.0
    jump_duration: Duration,
    jump_start_time: Option<Instant>,

    // Enemy properties
    enemy_frame_duration: Duration,
    enemy_frames: usize,
    enemy_route_duration: Duration,
}

impl GameController {
    pub fn new() -> Self {
        GameController {
            canvas_width: 0.0,
            canvas_height: 0.0,
            player_sprite_width: 0.0,
            player_sprite_height: 0.0,
            enemy_sprite_width: 0.0,
            enemy_sprite_height: 0.0,
            player_frame_duration: Duration::from_millis(50), // 50 ms per frame
            game_start_time: None,
            player_frames: 0,
            jump_height: 0.5, // default max jump height factor
            jump_duration: Duration::from_millis(3000), // 1.5 sec jump duration
            jump_start_time: None,
            enemy_frame_duration: Duration::from_millis(50),
            enemy_frames: 0,
            enemy_route_duration: Duration::from_secs(5),
        }
    }

    pub fn player_state(&mut self) -> PlayerState {

        if self.detect_collision() {
            self.game_start_time = None; // Reset game start time upon collision
            PlayerState::Dead
        } else {
            PlayerState::Live
        }
    }

fn detect_collision(&self) -> bool {
    if self.game_start_time.is_none() {
        return false;
    }

    let player_x1 = self.player_x();
    let player_x2 = player_x1 + self.player_sprite_width;
    let player_y1 = self.player_y();
    let player_y2 = player_y1 + self.player_sprite_height;

    let enemy_x1 = self.enemy_x();
    let enemy_x2 = enemy_x1 + self.enemy_sprite_width;
    let enemy_y1 = self.enemy_y();
    let enemy_y2 = enemy_y1 + self.enemy_sprite_height;

    let x_collision = player_x1 < enemy_x2 && player_x2 > enemy_x1;
    let y_collision = player_y1 < enemy_y2 && player_y2 > enemy_y1;

    x_collision && y_collision
}


    pub fn jump(&mut self) {
        if let Some(start_time) = self.jump_start_time {
            if start_time.elapsed() < self.jump_duration {
                return; // Ignore the new jump command
            }
        }
        self.jump_start_time = Some(Instant::now());
    }

    pub fn start(&mut self) {
        self.game_start_time = Some(Instant::now());
    }

    pub fn set_canvas_dimensions(&mut self, width: f32, height: f32) {
        self.canvas_width = width;
        self.canvas_height = height;
    }

    pub fn load_player_sprite_table(&mut self, total_frames: usize, sprite_width: f32, sprite_height: f32) {
        self.player_frames = total_frames;
        self.set_player_sprite_dimensions(sprite_width, sprite_height);
    }

    pub fn set_player_sprite_dimensions(&mut self, width: f32, height: f32) {
        self.player_sprite_width = width;
        self.player_sprite_height = height;
    }

    pub fn load_enemy_sprite_table(&mut self, total_frames: usize, sprite_width: f32, sprite_height: f32) {
        self.enemy_frames = total_frames;
        self.set_enemy_sprite_dimensions(sprite_width, sprite_height);
    }

    pub fn set_enemy_sprite_dimensions(&mut self, width: f32, height: f32) {
        self.enemy_sprite_width = width;
        self.enemy_sprite_height = height;
    }

    pub fn get_player_frame(&self) -> usize {
            if let Some(start_time) = self.game_start_time {
                let elapsed = start_time.elapsed();
                let frame_count = (elapsed.as_millis() / self.player_frame_duration.as_millis()) as usize;
                frame_count % self.player_frames
            } else {
                0
            }
        }

    pub fn player_x(&self) -> f32 {
        (self.canvas_width / 2.0) - (self.player_sprite_width / 2.0)
    }

    pub fn player_y(&self) -> f32 {
        let default_y = (self.canvas_height / 2.0) - (self.player_sprite_height / 2.0);

        if let Some(start_time) = self.jump_start_time {
            let elapsed = start_time.elapsed();

            // If the jump duration is complete, return to the default y position
            if elapsed >= self.jump_duration {
                return default_y;
            }

            // Normalize `t` to range from 0.0 to 1.0 over the jump duration
            let t = elapsed.as_secs_f32() / self.jump_duration.as_secs_f32();

            // Calculate the peak height of the jump (occurs at the midpoint of the jump duration)
            let jump_peak_y = default_y - (self.jump_height * default_y);

            // Parabolic interpolation for a smooth ascent and descent
            return default_y + (jump_peak_y - default_y) * (1.0 - 4.0 * (t - 0.5).powi(2));
        }

        // Return `default_y` if no jump is active
        default_y
    }

    pub fn get_enemy_frame(&self) -> usize {
        if let Some(start_time) = self.game_start_time {
            let elapsed = start_time.elapsed();
            let frame_count = (elapsed.as_millis() / self.enemy_frame_duration.as_millis()) as usize;
            frame_count % self.enemy_frames
        } else {
            0
        }
    }

    pub fn enemy_x(&self) -> f32 {
        if let Some(start_time) = self.game_start_time {
            let elapsed_secs = start_time.elapsed().as_secs_f32();
            let route_duration_secs = self.enemy_route_duration.as_secs_f32();
            let t = (elapsed_secs % route_duration_secs) / route_duration_secs;
            let start_x = self.canvas_width;
            let end_x = 0.0 - self.enemy_sprite_width;
            start_x + (end_x - start_x) * t
        } else {
            self.canvas_width
        }
    }

    pub fn enemy_y(&self) -> f32 {
        (self.canvas_height / 2.0) - (self.enemy_sprite_height / 2.0)
    }
}
