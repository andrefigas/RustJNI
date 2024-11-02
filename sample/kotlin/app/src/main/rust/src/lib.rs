use jni::JNIEnv;
use jni::objects::JClass;
//<RustJNI>
// primitive imports
use jni::sys::{jstring};
//</RustJNI>        
#[no_mangle]
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_sayHello(
    env: JNIEnv,
    _class: JClass,
    
) -> jstring {
    println!("Parameters: {:?}", ());

    let output = r#"Rust Method: sayHello"#;
env.new_string(output)
    .expect("Couldn't create Java string!")
    .into_raw()
}