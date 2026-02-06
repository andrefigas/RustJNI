use jni::JNIEnv;
use jni::objects::{JClass, JString};
//<RustJNI>
// primitive imports
use jni::sys::jstring;
//</RustJNI>

use serde::Deserialize;

#[derive(Deserialize)]
struct DogResponse {
    message: serde_json::Value,
    status: String,
}

fn build_runtime() -> Result<tokio::runtime::Runtime, String> {
    tokio::runtime::Builder::new_current_thread()
        .enable_all()
        .build()
        .map_err(|e| format!("Failed to create Tokio runtime: {}", e))
}

fn error_json(msg: &str) -> String {
    format!(r#"{{"status":"error","message":"{}"}}"#, msg.replace('"', "'"))
}

async fn http_get(url: &str) -> Result<String, String> {
    let response = reqwest::get(url)
        .await
        .map_err(|e| format!("Request failed: {}", e))?;

    response
        .text()
        .await
        .map_err(|e| format!("Failed to read body: {}", e))
}

#[no_mangle]
pub extern "C" fn Java_com_devfigas_rustjni_restapi_MainActivity_getRandomDog(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let result = match build_runtime() {
        Ok(rt) => rt.block_on(async {
            match http_get("https://dog.ceo/api/breeds/image/random").await {
                Ok(body) => body,
                Err(e) => error_json(&e),
            }
        }),
        Err(e) => error_json(&e),
    };

    env.new_string(result)
        .expect("Couldn't create Java string!")
        .into_raw()
}

#[no_mangle]
pub extern "C" fn Java_com_devfigas_rustjni_restapi_MainActivity_listBreeds(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let result = match build_runtime() {
        Ok(rt) => rt.block_on(async {
            match http_get("https://dog.ceo/api/breeds/list/all").await {
                Ok(body) => {
                    // Parse and return only breed names for readability
                    match serde_json::from_str::<DogResponse>(&body) {
                        Ok(parsed) => {
                            if let Some(obj) = parsed.message.as_object() {
                                let breeds: Vec<&String> = obj.keys().collect();
                                match serde_json::to_string_pretty(&breeds) {
                                    Ok(json) => json,
                                    Err(_) => body,
                                }
                            } else {
                                body
                            }
                        }
                        Err(_) => body,
                    }
                }
                Err(e) => error_json(&e),
            }
        }),
        Err(e) => error_json(&e),
    };

    env.new_string(result)
        .expect("Couldn't create Java string!")
        .into_raw()
}

#[no_mangle]
pub extern "C" fn Java_com_devfigas_rustjni_restapi_MainActivity_getBreedImage(
    mut env: JNIEnv,
    _class: JClass,
    breed: jstring,
) -> jstring {
    let breed_str: String = match env.get_string(&unsafe { JString::from_raw(breed) }) {
        Ok(s) => s.into(),
        Err(_) => {
            let err = error_json("Failed to read breed parameter");
            return env.new_string(err)
                .expect("Couldn't create Java string!")
                .into_raw();
        }
    };

    let url = format!("https://dog.ceo/api/breed/{}/images/random", breed_str);

    let result = match build_runtime() {
        Ok(rt) => rt.block_on(async {
            match http_get(&url).await {
                Ok(body) => body,
                Err(e) => error_json(&e),
            }
        }),
        Err(e) => error_json(&e),
    };

    env.new_string(result)
        .expect("Couldn't create Java string!")
        .into_raw()
}
