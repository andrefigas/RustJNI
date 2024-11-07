use jni::JNIEnv;
use jni::objects::JClass;
//<RustJNI>
// primitive imports
use jni::sys::{jint, jboolean, jfloat};
//</RustJNI>
use crate::game_controller::GameController;
use crate::player_state::PlayerState;
use std::sync::Mutex;
use lazy_static::lazy_static;

mod game_controller;
mod player_state;

lazy_static! {
    static ref GAME_CONTROLLER: Mutex<GameController> = Mutex::new(GameController::new());
}

#[no_mangle]
pub extern "C" fn Java_com_devfigas_gamesample_NativeLib_loadCanvas(
    _env: JNIEnv,
    _class: JClass,
    width: jint,
    height: jint,
) {
    println!("Canvas loaded with width: {} and height: {}", width, height);
    let mut controller = GAME_CONTROLLER.lock().unwrap();
    controller.set_canvas_dimensions(width as f32, height as f32);
}

#[no_mangle]
pub extern "C" fn Java_com_devfigas_gamesample_NativeLib_loadEnemySpriteTable(
    _env: JNIEnv,
    _class: JClass,
    size: jint,
    height: jint,
    width: jint,
) {
    println!("Loading enemy sprite table with size: {}, height: {}, width: {}", size, height, width);
    let mut controller = GAME_CONTROLLER.lock().unwrap();
    controller.load_enemy_sprite_table(size as usize, width as f32, height as f32);
}

#[no_mangle]
pub extern "C" fn Java_com_devfigas_gamesample_NativeLib_loadPlayerSpriteTable(
    _env: JNIEnv,
    _class: JClass,
    size: jint,
    height: jint,
    width: jint,
) {
    println!("Loading run sprite table with size: {}, height: {}, width: {}", size, height, width);
    let mut controller = GAME_CONTROLLER.lock().unwrap();
    controller.load_player_sprite_table(size as usize, width as f32, height as f32);
}

#[no_mangle]
pub extern "C" fn Java_com_devfigas_gamesample_NativeLib_actionJump(
    _env: JNIEnv,
    _class: JClass,
) {
    let mut controller = GAME_CONTROLLER.lock().unwrap();
    controller.jump();
}

#[no_mangle]
pub extern "C" fn Java_com_devfigas_gamesample_NativeLib_actionStart(
    _env: JNIEnv,
    _class: JClass,
) {
    let mut controller = GAME_CONTROLLER.lock().unwrap();
    controller.start();
}

#[no_mangle]
pub extern "C" fn Java_com_devfigas_gamesample_NativeLib_isDead(
    _env: JNIEnv,
    _class: JClass,
) -> jboolean {
    let mut controller = GAME_CONTROLLER.lock().unwrap();
    if controller.player_state() == PlayerState::Dead {
        jni::sys::JNI_TRUE
    } else {
        jni::sys::JNI_FALSE
    }
}

#[no_mangle]
pub extern "C" fn Java_com_devfigas_gamesample_NativeLib_getPlayerX(
    _env: JNIEnv,
    _class: JClass,
) -> jfloat {
    let controller = GAME_CONTROLLER.lock().unwrap();
    controller.player_x()
}

#[no_mangle]
pub extern "C" fn Java_com_devfigas_gamesample_NativeLib_getPlayerY(
    _env: JNIEnv,
    _class: JClass,
) -> jfloat {
    let controller = GAME_CONTROLLER.lock().unwrap();
    controller.player_y()
}

#[no_mangle]
pub extern "C" fn Java_com_devfigas_gamesample_NativeLib_getEnemyX(
    _env: JNIEnv,
    _class: JClass,
) -> jfloat {
    let controller = GAME_CONTROLLER.lock().unwrap();
    controller.enemy_x()
}

#[no_mangle]
pub extern "C" fn Java_com_devfigas_gamesample_NativeLib_getEnemyY(
    _env: JNIEnv,
    _class: JClass,
) -> jfloat {
    let controller = GAME_CONTROLLER.lock().unwrap();
    controller.enemy_y()
}

#[no_mangle]
pub extern "C" fn Java_com_devfigas_gamesample_NativeLib_getPlayerSprite(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    let controller = GAME_CONTROLLER.lock().unwrap();
    controller.get_player_frame() as jint
}

#[no_mangle]
pub extern "C" fn Java_com_devfigas_gamesample_NativeLib_getEnemySprite(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    println!("Fetching enemy sprite");
    let controller = GAME_CONTROLLER.lock().unwrap();
    controller.get_enemy_frame() as jint
}