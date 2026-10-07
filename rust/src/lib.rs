use jni::objects::{JClass, JString};
use jni::sys::jstring;
use jni::JNIEnv;

#[no_mangle]
pub extern "system" fn Java_com_liaojinxuan_tilivili_MainActivity_helloRust(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let output = env.new_string("Hello from Rust!").expect("Couldn't create java string!");
    output.into_raw()
}

// 接收 img_key, sub_key, 以及拼接好的参数串，返回 w_rid 签名
#[no_mangle]
pub extern "system" fn Java_com_liaojinxuan_tilivili_MainActivity_wbiSign(
    mut env: JNIEnv,
    _class: JClass,
    img_key: JString,
    sub_key: JString,
    raw_query: JString,
) -> jstring {
    let img_key: String = env.get_string(&img_key).expect("Couldn't get img_key").into();
    let sub_key: String = env.get_string(&sub_key).expect("Couldn't get sub_key").into();
    let raw_query: String = env.get_string(&raw_query).expect("Couldn't get raw_query").into();

    // 1. 拼接原始的 mixin_key
    let mixin_key = format!("{}{}", img_key, sub_key);

    // 2. 按 B站算法，用 64 位重排表对 mixin_key 进行重新排列
    let mut mixin_key_chars: Vec<char> = mixin_key.chars().collect();
    let mut reordered = String::new();
    let table = [
        46, 47, 18, 2, 53, 8, 23, 32, 15, 50, 10, 31, 58, 3, 45, 35,
        27, 43, 5, 49, 33, 9, 42, 19, 29, 28, 14, 39, 12, 38, 41, 13,
        37, 48, 7, 16, 24, 55, 40, 61, 26, 17, 0, 1, 60, 51, 30, 4,
        22, 25, 54, 21, 56, 59, 6, 63, 57, 62, 11, 36, 20, 34, 44, 52,
    ];
    for &idx in table.iter() {
        if idx < mixin_key_chars.len() {
            reordered.push(mixin_key_chars[idx]);
        }
    }
    // 3. 取前32位作为最终的 mixin_key
    let final_mixin_key = &reordered[..32];

    // 4. 拼接待签名字符串
    let sign_str = format!("{}{}", raw_query, final_mixin_key);
    
    // 5. 计算 MD5
    let digest = md5::compute(sign_str.as_bytes());
    let sign = format!("{:x}", digest);

    let output = env.new_string(sign).expect("Couldn't create java string!");
    output.into_raw()
}