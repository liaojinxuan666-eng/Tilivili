use jni::objects::{JClass, JString};
use jni::sys::jstring;
use jni::JNIEnv;
use md5::{Digest, Md5};

#[no_mangle]
pub extern "system" fn Java_com_liaojinxuan_tilivili_MainActivity_helloRust(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let output = env
        .new_string("Hello from Rust!")
        .expect("Couldn't create java string!");
    output.into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_liaojinxuan_tilivili_MainActivity_wbiSign(
    env: JNIEnv,
    _class: JClass,
    raw_query: JString,
) -> jstring {
    let query: String = env
        .get_string(&raw_query)
        .expect("Couldn't get java string!")
        .into();

    let mixed = format!("{}ea1db124af3c7062474693fa704f4ff8", query);
    let mut hasher = Md5::new();
    hasher.update(mixed.as_bytes());
    let result = hasher.finalize();
    let sign = hex::encode(result);

    let output = env
        .new_string(sign)
        .expect("Couldn't create java string!");
    output.into_raw()
}