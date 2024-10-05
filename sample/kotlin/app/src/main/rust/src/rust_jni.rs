use jni::JNIEnv;
use jni::objects::JClass;
//<RustJNI>
// primitive imports
use jni::sys::{jint, jstring};
//</RustJNI>        
#[no_mangle]
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod(
    env: JNIEnv,
    _class: JClass,
    param0: jint
) -> jstring {
    println!("Parameters: {:?}", (param0));

    let output = r#"Rust Method: someMethod"#;
env.new_string(output)
    .expect("Couldn't create Java string!")
    .into_inner()
}