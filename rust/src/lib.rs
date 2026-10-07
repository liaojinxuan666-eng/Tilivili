use jni::objects::{JClass, JString};
use jni::sys::jstring;
use jni::JNIEnv;
use md5::{Digest, Md5};

#[no_mangle]
pub extern "system" fn Java_com_liaojinxuan_tilivili_MainActivity_helloRust(
    mut env: JNIEnv,
    _class: JClass,
) -> jstring {
    let output = env
        .new_string("Hello from Rust!")
        .expect("Couldn't create java string!");
    output.into_raw()
}

// 新增：生成 Wbi 签名
#[no_mangle]
pub extern "system" fn Java_com_liaojinxuan_tilivili_MainActivity_wbiSign(
    mut env: JNIEnv,
    _class: JClass,
    raw_query: JString,
) -> jstring {
    // 将 Kotlin 传过来的字符串转换为 Rust String
    let query: String = env
        .get_string(&raw_query)
        .expect("Couldn't get java string!")
        .into();

    // 模拟 Wbi 签名过程（实际算法需要结合具体参数，这里先用一个简化的 Hash 演示）
    // 真实场景会先用一个固定的 salt（如 "ea1db124af3c7062474693fa704f4ff8"）
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