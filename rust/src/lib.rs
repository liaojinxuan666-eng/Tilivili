use jni::objects::{JClass, JString};
use jni::sys::jstring;
use jni::JNIEnv;

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

    // 模拟 Wbi 签名，真实场景中这里的 salt 需要动态获取
    let mixed = format!("{}ea1db124af3c7062474693fa704f4ff8", query);
    
    // md5 0.7.0 的用法：直接 compute，然后用 {:x} 格式化输出十六进制
    let digest = md5::compute(mixed.as_bytes());
    let sign = format!("{:x}", digest);

    let output = env
        .new_string(sign)
        .expect("Couldn't create java string!");
    output.into_raw()
}