use jni::JNIEnv;
use jni::objects::JClass;
//<RustJNI>
// primitive imports
use jni::sys::{jfloat, jint};
//</RustJNI>

#[no_mangle]
pub extern "C" fn Java_com_example_flappybirdnative_MainActivity_onInput(
    _env: JNIEnv,
    _class: JClass,
) {
    flappy_core::game_on_input();
}

#[no_mangle]
pub extern "C" fn Java_com_example_flappybirdnative_MainActivity_update(
    _env: JNIEnv,
    _class: JClass,
    dt: jfloat,
) {
    flappy_core::game_update(dt as f64);
}

#[no_mangle]
pub extern "C" fn Java_com_example_flappybirdnative_MainActivity_resetGame(
    _env: JNIEnv,
    _class: JClass,
) {
    flappy_core::game_reset();
}

#[no_mangle]
pub extern "C" fn Java_com_example_flappybirdnative_MainActivity_getState(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    flappy_core::game_get_state()
}

#[no_mangle]
pub extern "C" fn Java_com_example_flappybirdnative_MainActivity_getBirdY(
    _env: JNIEnv,
    _class: JClass,
) -> jfloat {
    flappy_core::game_get_bird_y() as f32
}

#[no_mangle]
pub extern "C" fn Java_com_example_flappybirdnative_MainActivity_getPipeCount(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    flappy_core::game_get_pipe_count()
}

#[no_mangle]
pub extern "C" fn Java_com_example_flappybirdnative_MainActivity_getPipeX(
    _env: JNIEnv,
    _class: JClass,
    index: jint,
) -> jfloat {
    flappy_core::game_get_pipe_x(index) as f32
}

#[no_mangle]
pub extern "C" fn Java_com_example_flappybirdnative_MainActivity_getPipeGapY(
    _env: JNIEnv,
    _class: JClass,
    index: jint,
) -> jfloat {
    flappy_core::game_get_pipe_gap_y(index) as f32
}

#[no_mangle]
pub extern "C" fn Java_com_example_flappybirdnative_MainActivity_getCanvasWidth(
    _env: JNIEnv,
    _class: JClass,
) -> jfloat {
    flappy_core::game_get_canvas_width() as f32
}

#[no_mangle]
pub extern "C" fn Java_com_example_flappybirdnative_MainActivity_getCanvasHeight(
    _env: JNIEnv,
    _class: JClass,
) -> jfloat {
    flappy_core::game_get_canvas_height() as f32
}

