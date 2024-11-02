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
    let output = r#"
__________________________
< Hello RustJNI >
--------------------------
        \\
         \\
            _~^~^~_
        \\) /  o o  \\ (/
          '_   -   _'
          / '-----' \\
_________________________________________________________
Do your rust implementation there: /rust/src/lib.rs
---------------------------------------------------------"#;

    env.new_string(output)
        .expect("Couldn't create Java string!")
        .into_raw()
}